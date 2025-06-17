package com.contract.backend.service;

import com.contract.backend.common.dto.BlockchainMetadataDTO;
import com.fasterxml.jackson.databind.ObjectMapper; // ObjectMapper 사용
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; // JavaTimeModule 사용
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import org.hyperledger.fabric.client.CommitException;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.EndorseException;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.SubmitException;
import org.hyperledger.fabric.client.identity.Identities;
import org.hyperledger.fabric.client.identity.Identity;
import org.hyperledger.fabric.client.identity.Signer;
import org.hyperledger.fabric.client.identity.Signers;
import org.hyperledger.fabric.client.identity.X509Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@Service("blockchainService") // Qualifier 이름 지정
public class HyperledgerFabricService implements BlockchainService {

    private static final Logger logger = LoggerFactory.getLogger(HyperledgerFabricService.class);
    private final ObjectMapper objectMapper; // Gson 대신 ObjectMapper 사용

    @Value("${fabric.mspId}")
    private String mspId;

    @Value("${fabric.channelName}")
    private String channelName;

    @Value("${fabric.chaincodeName}")
    private String chaincodeName;

    @Value("${fabric.credentials.certificatePath}")
    private String certificatePathString;

    @Value("${fabric.credentials.privateKeyPath}")
    private String privateKeyPathString;

    @Value("${fabric.gateway.peerEndpoint}")
    private String peerEndpoint;

    @Value("${fabric.gateway.tlsCaCertPath}")
    private String tlsCaCertPathString;

    @Value("${fabric.gateway.overrideAuth:#{null}}")
    private String overrideAuth;

    public HyperledgerFabricService(ObjectMapper objectMapper) { // ObjectMapper 주입
        this.objectMapper = objectMapper.copy(); // 원본 ObjectMapper의 설정을 복사하여 사용
        this.objectMapper.registerModule(new JavaTimeModule()); // 날짜/시간 모듈 등록
    }

    private Gateway connectGateway() throws IOException, CertificateException, InvalidKeyException {
        Path certificatePath = Paths.get(certificatePathString);
        X509Certificate certificate = Identities.readX509Certificate(Files.newBufferedReader(certificatePath));

        Path privateKeyPath = Paths.get(privateKeyPathString);
        PrivateKey privateKey = Identities.readPrivateKey(Files.newBufferedReader(privateKeyPath));

        Identity identity = new X509Identity(mspId, certificate);
        Signer signer = Signers.newPrivateKeySigner(privateKey);

        Path tlsCaCertPath = Paths.get(tlsCaCertPathString);
        InputStream tlsCaCertInputStream = Files.newInputStream(tlsCaCertPath);

        ManagedChannel grpcChannel = NettyChannelBuilder.forTarget(peerEndpoint)
                .sslContext(GrpcSslContexts.forClient().trustManager(tlsCaCertInputStream).build())
                .overrideAuthority(overrideAuth != null ? overrideAuth : peerEndpoint.split(":")[0])
                .build();

        logger.info("gRPC Channel built for endpoint: {}", peerEndpoint);

        return Gateway.newInstance()
                .identity(identity)
                .signer(signer)
                .connection(grpcChannel)
                .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
                .submitOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
                .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES))
                .connect();
    }

    @Override
    public String recordContractVersionMetadata(BlockchainMetadataDTO metadataDto) throws Exception {
        String transactionId = "UNKNOWN_TX_ID"; // 기본값 설정
        try (Gateway gateway = connectGateway()) {
            org.hyperledger.fabric.client.Network network = gateway.getNetwork(channelName);
            Contract contract = network.getContract(chaincodeName);

            String metadataJsonString = objectMapper.writeValueAsString(metadataDto);
            logger.info("Submitting transaction to chaincode function 'CreateContractMetadataRecord' with metadata: {}", metadataJsonString);

            String recordId = "CONTRACT_VERSION_" + metadataDto.getContractVersionId();

            byte[] result = contract.submitTransaction("CreateContractMetadataRecord", recordId, metadataJsonString);
            // 체인코드가 트랜잭션 ID를 응답으로 반환한다고 가정
            if (result != null && result.length > 0) {
                transactionId = new String(result, StandardCharsets.UTF_8);
            } else {
                // submitTransaction 자체가 성공하면 트랜잭션은 제출된 것입니다.
                // 실제 트랜잭션 ID는 이벤트 또는 다른 방식으로 얻어야 할 수 있으나, 여기서는 체인코드 반환값을 사용합니다.
                // 만약 반환값이 없다면, 성공했다는 의미로 임의의 ID나 성공 메시지를 사용할 수 있습니다.
                // 여기서는 Gateway SDK v1.x에서 submitTransaction이 byte[]를 반환하고,
                // 이 값이 체인코드에서 명시적으로 반환된 값이라고 가정합니다.
                // 많은 체인코드 구현에서 트랜잭션 ID를 직접 반환하지 않고, 호출한 클라이언트가
                // 트랜잭션 제출 후 별도로 조회하거나, SDK 내부적으로 처리된 ID를 사용합니다.
                // Hyperledger Fabric Client SDK (레거시)는 Transaction ID를 미리 생성하지만, Gateway는 다릅니다.
                // 여기서는 submitTransaction의 반환값이 비어있다면, 임시 ID를 사용합니다.
                transactionId = "SUBMITTED_SUCCESSFULLY_" + System.currentTimeMillis(); // 임시 ID
            }
            logger.info("Transaction submitted successfully. Chaincode response/TxID: {}", transactionId);
            return transactionId;

        } catch (EndorseException | SubmitException | CommitException e) {
            logger.error("Error during fabric transaction: {}", e.getMessage(), e);
            // e.getDetails() 등을 통해 체인코드 에러 메시지 확인 가능
            if (e instanceof SubmitException) {
                transactionId = ((SubmitException) e).getTransactionId();
                logger.error("SubmitException Transaction ID: {}", transactionId);
            }
            throw new RuntimeException("Fabric transaction failed: " + e.getMessage() + (transactionId.startsWith("UNKNOWN") ? "" : " (TxID: " + transactionId + ")"), e);
        } catch (GatewayException e) {
            logger.error("Gateway connection error: {}", e.getMessage(), e);
            throw new RuntimeException("Fabric gateway connection failed: " + e.getMessage(), e);
        } catch (IOException | CertificateException | InvalidKeyException e) {
            logger.error("Fabric client setup error: {}", e.getMessage(), e);
            throw new RuntimeException("Fabric client setup error: " + e.getMessage(), e);
        }
    }

    @Override
    public BlockchainMetadataDTO getContractMetadataFromBlockchain(Long contractVersionDbId) throws Exception {
        String recordKey = "CONTRACT_VERSION_" + contractVersionDbId;
        try (Gateway gateway = connectGateway()) {
            org.hyperledger.fabric.client.Network network = gateway.getNetwork(channelName);
            Contract contract = network.getContract(chaincodeName);

            logger.info("Querying chaincode function 'ReadContractMetadataRecord' with key: {}", recordKey);

            byte[] resultBytes = contract.evaluateTransaction("ReadContractMetadataRecord", recordKey);

            if (resultBytes == null || resultBytes.length == 0) {
                logger.warn("No metadata found on blockchain for key: {}", recordKey);
                return null;
            }

            String metadataJsonString = new String(resultBytes, StandardCharsets.UTF_8);
            logger.info("Received metadata from blockchain: {}", metadataJsonString);

            return objectMapper.readValue(metadataJsonString, BlockchainMetadataDTO.class);

        } catch (GatewayException e) {
            logger.error("Fabric gateway error while querying metadata for key {}: {}", recordKey, e.getMessage(), e);
            // 체인코드에서 "자산 찾을 수 없음" 오류를 명시적으로 발생시키는 경우, 여기서 그 원인을 파악하여 null 반환 가능
            if (e.getStatus() != null && e.getMessage().toUpperCase().contains("ASSET_NOT_FOUND")) { // 예시: 오류 메시지 확인
                logger.warn("Asset not found on blockchain for key {} (gateway exception)", recordKey);
                return null;
            }
            throw new RuntimeException("Fabric gateway error: " + e.getMessage(), e);
        } catch (IOException | CertificateException | InvalidKeyException e) {
            logger.error("Fabric client setup error: {}", e.getMessage(), e);
            throw new RuntimeException("Fabric client setup error: " + e.getMessage(), e);
        }
    }
}