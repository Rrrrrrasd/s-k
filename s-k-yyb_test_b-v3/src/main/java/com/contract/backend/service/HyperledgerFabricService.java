package com.contract.backend.service;

import com.contract.backend.common.Entity.ContractVersionEntity;
import com.contract.backend.common.Entity.UserEntity;
import com.contract.backend.common.dto.BlockchainMetadataDTO;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import com.google.gson.Gson; // JSON 직렬화를 위해 Gson 또는 Jackson 사용 가능
import com.google.gson.JsonObject;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class HyperledgerFabricService implements BlockchainService {

    private static final Logger logger = LoggerFactory.getLogger(HyperledgerFabricService.class);
    private final Gson gson = new Gson(); // JSON 변환용

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

    @Value("${fabric.gateway.overrideAuth:#{null}}") // 값이 없으면 null
    private String overrideAuth;


    private Gateway connectGateway() throws IOException, CertificateException, InvalidKeyException {
        // Load credentials
        Path certificatePath = Paths.get(certificatePathString);
        X509Certificate certificate = Identities.readX509Certificate(Files.newBufferedReader(certificatePath));

        Path privateKeyPath = Paths.get(privateKeyPathString);
        PrivateKey privateKey = Identities.readPrivateKey(Files.newBufferedReader(privateKeyPath));

        Identity identity = new X509Identity(mspId, certificate);
        Signer signer = Signers.newPrivateKeySigner(privateKey);

        // TLS CA Certificate
        Path tlsCaCertPath = Paths.get(tlsCaCertPathString);
        InputStream tlsCaCertInputStream = Files.newInputStream(tlsCaCertPath);


        ManagedChannel grpcChannel = NettyChannelBuilder.forTarget(peerEndpoint)
                .sslContext(GrpcSslContexts.forClient().trustManager(tlsCaCertInputStream).build())
                .overrideAuthority(overrideAuth != null ? overrideAuth : peerEndpoint.split(":")[0]) // overrideAuth가 null이면 endpoint의 호스트 부분 사용
                .build();

        logger.info("gRPC Channel built for endpoint: {}", peerEndpoint);

        return Gateway.newInstance()
                .identity(identity)
                .signer(signer)
                .connection(grpcChannel)
                .evaluateOptions(options -> options.withDeadlineAfter(5, TimeUnit.SECONDS))
                .endorseOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS))
                .submitOptions(options -> options.withDeadlineAfter(15, TimeUnit.SECONDS)) // 트랜잭션 제출 타임아웃 증가
                .commitStatusOptions(options -> options.withDeadlineAfter(1, TimeUnit.MINUTES))
                .connect();
    }

    @Override
    public String recordContractVersionMetadata(ContractVersionEntity contractVersion, List<UserEntity> signers) throws Exception {
        String transactionId = null;
        try (Gateway gateway = connectGateway()) {
            org.hyperledger.fabric.client.Network network = gateway.getNetwork(channelName);
            Contract contract = network.getContract(chaincodeName);

            // 블록체인에 저장할 메타데이터 구성
            JsonObject metadata = new JsonObject();
            metadata.addProperty("contractDbId", contractVersion.getContract().getId());
            metadata.addProperty("contractVersionDbId", contractVersion.getId());
            metadata.addProperty("versionNumber", contractVersion.getVersionNumber());
            metadata.addProperty("fileHash", contractVersion.getFileHash());
            metadata.addProperty("storageProvider", contractVersion.getStorageProvider());
            metadata.addProperty("bucketName", contractVersion.getBucketName());
            metadata.addProperty("filePath", contractVersion.getFilePath());
            metadata.addProperty("status", contractVersion.getStatus().name()); // VersionStatus.SIGNED
            metadata.addProperty("finalizedAt", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            List<String> signerUuids = signers.stream().map(UserEntity::getUuid).collect(Collectors.toList());
            metadata.add("signerUuids", gson.toJsonTree(signerUuids));

            String metadataJsonString = gson.toJson(metadata);
            logger.info("Submitting transaction to chaincode function 'CreateContractMetadataRecord' with metadata: {}", metadataJsonString);

            // 체인코드 함수 호출 (예: "CreateContractMetadataRecord")
            // 함수명과 인자는 실제 체인코드에 맞게 수정해야 합니다.
            // 첫번째 인자는 보통 Key로 사용될 수 있는 고유 ID, 두번째 인자가 실제 데이터
            String recordId = "CONTRACT_VERSION_" + contractVersion.getId();

            // Submit transaction
            byte[] result = contract.submitTransaction("CreateContractMetadataRecord", recordId, metadataJsonString);
            transactionId = StandardCharsets.UTF_8.decode(java.nio.ByteBuffer.wrap(result)).toString(); // 체인코드가 트랜잭션 ID를 반환한다고 가정

            // submitTransaction은 commit까지 완료된 후 결과를 반환합니다.
            // 성공 시, 체인코드가 반환하는 byte[]를 파싱하여 트랜잭션 ID 등을 얻을 수 있습니다.
            // 여기서는 체인코드가 생성된 레코드의 ID나 트랜잭션 ID 자체를 문자열로 반환한다고 가정합니다.
            // 실제로는 체인코드의 응답에 따라 파싱 방식이 달라집니다.
            // Fabric Gateway v1.x 에서는 submitTransaction()이 void를 반환하지 않고 byte[]를 반환하므로,
            // 체인코드에서 반환하는 값을 받아 처리할 수 있습니다.
            // 일반적으로 transactionId는 submitTransaction 호출 전에 알 수 없고, 호출 후 event listener를 통해 얻거나,
            // client 라이브러리가 내부적으로 처리해줍니다. 여기서는 submitTransaction의 반환값을 transactionId로 간주하는 예시입니다.
            // 더 정확하게는, submitTransaction 호출 자체가 성공하면 트랜잭션이 제출된 것이고,
            // 패브릭 네트워크에서 이 트랜잭션의 ID를 특정하는 방법은 체인코드 이벤트 또는 SDK 기능에 따라 다릅니다.
            // Gateway SDK에서는 별도로 transaction ID를 명시적으로 얻는 API보다는 submit 성공 여부로 판단합니다.
            // 여기서는 체인코드 함수가 응답으로 트랜잭션 ID를 반환한다고 가정하고, 이를 사용합니다.
            // 만약 체인코드가 다른 값을 반환한다면, 그 값을 transactionId 대신 사용하거나 로깅합니다.

            logger.info("Transaction submitted successfully. Chaincode response (assumed TxID or record ID): {}", transactionId);

        } catch (EndorseException | SubmitException | CommitException e) {
            logger.error("Error during fabric transaction: {}", e.getMessage(), e);
            // e.getStatus() 등을 통해 상세한 오류 정보 확인 가능
            // e.getDetails() 등으로 체인코드에서 발생한 오류 메시지 확인 가능
            throw new RuntimeException("Fabric transaction failed: " + e.getMessage(), e);
        } catch (GatewayException e) {
            logger.error("Gateway connection error: {}", e.getMessage(), e);
            throw new RuntimeException("Fabric gateway connection failed: " + e.getMessage(), e);
        } catch (IOException | CertificateException | InvalidKeyException e) {
            logger.error("Fabric client setup error: {}", e.getMessage(), e);
            throw new RuntimeException("Fabric client setup error: " + e.getMessage(), e);
        } finally {
            // gRPC 채널은 Gateway.close() 시 자동으로 닫힙니다.
        }
        // 실제 트랜잭션 ID를 반환하도록 수정 필요 (submitTransaction의 반환값 또는 다른 방식)
        // 임시로, 성공 시 기록된 ID를 반환하거나, 체인코드에서 반환하는 값을 사용
        return transactionId != null ? transactionId : "SIMULATED_TX_ID_" + System.currentTimeMillis(); // 임시 반환값
    }

    @Override
    public BlockchainMetadataDTO getContractMetadataFromBlockchain(Long contractVersionDbId) throws Exception {
        String recordKey = null;
        try (Gateway gateway = connectGateway()) {
            org.hyperledger.fabric.client.Network network = gateway.getNetwork(channelName);
            Contract contract = network.getContract(chaincodeName);

            // 체인코드에서 데이터를 조회할 때 사용할 키를 생성합니다.
            // recordContractVersionMetadata에서 사용한 키 생성 방식과 일치해야 합니다.
            // 예: "CONTRACT_VERSION_" + contractVersionDbId
            recordKey = "CONTRACT_VERSION_" + contractVersionDbId;

            logger.info("Querying chaincode function 'ReadContractMetadataRecord' with key: {}", recordKey);

            // 체인코드 함수 호출 (예: "ReadContractMetadataRecord" 또는 "QueryAsset")
            // 이 함수는 키를 인자로 받아 저장된 메타데이터(JSON 문자열)를 반환해야 합니다.
            // 조회 작업은 일반적으로 submitTransaction 대신 evaluateTransaction을 사용합니다 (원장에 쓰기 작업이 없는 경우).
            byte[] resultBytes = contract.evaluateTransaction("ReadContractMetadataRecord", recordKey);

            if (resultBytes == null || resultBytes.length == 0) {
                logger.warn("No metadata found on blockchain for key: {}", recordKey);
                return null; // 또는 예외 발생
            }

            String metadataJsonString = new String(resultBytes, StandardCharsets.UTF_8);
            logger.info("Received metadata from blockchain: {}", metadataJsonString);

            // JSON 문자열을 BlockchainMetadataDTO 객체로 변환
            // Gson 라이브러리를 사용합니다. (이미 멤버 변수로 gson이 선언되어 있다고 가정)
            BlockchainMetadataDTO metadataDTO = gson.fromJson(metadataJsonString, BlockchainMetadataDTO.class);

            // 만약 DB에 저장된 트랜잭션 ID도 DTO에 넣고 싶다면,
            // BlockchainRecordRepository를 주입받아 여기서 조회 후 설정할 수 있습니다. (선택 사항)

            return metadataDTO;

        } catch (GatewayException e) {
            // GatewayException은 다양한 원인(예: 체인코드에서 해당 키로 데이터 못 찾음, 연결 문제 등)으로 발생 가능
            // 체인코드에서 키를 못 찾을 경우 예외를 발생시키도록 설계했다면 여기서 잡힐 수 있습니다.
            // 또는 체인코드가 빈 값을 반환하고 여기서 null/빈 바이트 배열로 처리할 수도 있습니다.
            logger.error("Fabric gateway error while querying metadata for key {}: {}", recordKey, e.getMessage(), e);
            // e.getChaincodeEvents() 등으로 더 자세한 오류 정보 확인 가능
            if (e.getCause() != null && e.getCause().getMessage().contains("ASSET_NOT_FOUND")) { // 체인코드에서 특정 오류 메시지를 보낸 경우
                logger.warn("Asset not found on blockchain for key: {}", recordKey);
                return null;
            }
            throw new RuntimeException("Fabric gateway error: " + e.getMessage(), e);
        } catch (IOException | CertificateException | InvalidKeyException e) {
            logger.error("Fabric client setup error: {}", e.getMessage(), e);
            throw new RuntimeException("Fabric client setup error: " + e.getMessage(), e);
        }
        // catch (Exception e) { // 일반적인 예외 처리
        //     logger.error("Error querying metadata from blockchain for key {}: {}", recordKey, e.getMessage(), e);
        //     throw new RuntimeException("Error querying metadata: " + e.getMessage(), e);
        // }
    }

}