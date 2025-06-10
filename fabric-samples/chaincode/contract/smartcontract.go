// CreateContractMetadataRecord stores a new contract metadata record in the world state.
// id: a unique identifier for the metadata record (e.g., "CONTRACT_VERSION_" + contractVersion.getId())
// metadataJson: a JSON string containing the contract metadata
func (s *SmartContract) CreateContractMetadataRecord(ctx contractapi.TransactionContextInterface, id string, metadataJson string) error {
	// Check if the asset already exists
	exists, err := s.AssetExists(ctx, id) // AssetExists는 asset-transfer-basic의 기존 함수 활용
	if err != nil {
		return fmt.Errorf("failed to read from world state: %v", err)
	}
	if exists {
		// If we don't want to allow updates via this function, return an error.
		// Or, if updates are allowed, this could be the logic to update an existing record.
		// For now, let's assume new records should not overwrite existing ones with the same ID via this function.
		return fmt.Errorf("the metadata record %s already exists", id)
	}

	// Put the metadata JSON string into the world state
	err = ctx.GetStub().PutState(id, []byte(metadataJson))
	if err != nil {
		return fmt.Errorf("failed to put metadata record in world state: %v", err)
	}
	// Optionally, you can return a success message or the ID itself if needed,
	// but usually, returning nil on success is standard for create/update operations.
	// The transaction ID will be available to the client application from the SDK response.
	return nil
}


// ReadContractMetadataRecord retrieves a contract metadata record from the world state.
// id: the unique identifier for the metadata record
func (s *SmartContract) ReadContractMetadataRecord(ctx contractapi.TransactionContextInterface, id string) (string, error) {
	metadataBytes, err := ctx.GetStub().GetState(id)
	if err != nil {
		return "", fmt.Errorf("failed to read metadata record %s from world state: %v", id, err)
	}
	if metadataBytes == nil {
		// It's important to decide how to handle "not found".
		// Returning an error is one way. Returning an empty string or a specific DTO might be another.
		// The Java service (HyperledgerFabricService) should be prepared to handle this.
		// For example, it might throw a specific exception if the record is not found.
		return "", fmt.Errorf("metadata record %s does not exist", id)
	}

	// Return the metadata as a JSON string
	return string(metadataBytes), nil
}


// AssetExists returns true when asset with given ID exists in world state
func (s *SmartContract) AssetExists(ctx contractapi.TransactionContextInterface, id string) (bool, error) {
	assetJSON, err := ctx.GetStub().GetState(id)
	if err != nil {
		return false, fmt.Errorf("failed to read from world state: %v", err)
	}

	return assetJSON != nil, nil
}