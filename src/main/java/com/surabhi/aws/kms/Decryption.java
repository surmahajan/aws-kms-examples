package com.surabhi.aws.kms;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3Encryption;
import com.amazonaws.services.s3.AmazonS3EncryptionClientBuilder;
import com.amazonaws.services.s3.model.CryptoConfiguration;
import com.amazonaws.services.s3.model.KMSEncryptionMaterialsProvider;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;

/**
 * 
 * Client Side encryption - Decrypt the encrypted mails by SES
 * The emails are placed in the S3 bucket 
 *
 */
public class Decryption {

	public static void main(String str[]) throws IOException {

		String bucketName = "<BUCKET NAME>";
		String keyName = "<Encrypted OBJECT KEY NAME>";
		Regions clientRegion = Regions.US_EAST_1;
		String kms_id = "<KMS ID>";

		String FILEPATH = "decrypted-version.txt";
		File file = new File(FILEPATH);

		try {

			// Create the encryption client.
			KMSEncryptionMaterialsProvider materialProvider = new KMSEncryptionMaterialsProvider(kms_id);
			CryptoConfiguration cryptoConfig = new CryptoConfiguration()
					.withAwsKmsRegion(RegionUtils.getRegion(clientRegion.toString()));

			// Create S3 Encryption client.
			AmazonS3Encryption encryptionClient = AmazonS3EncryptionClientBuilder.standard()
					.withCredentials(new ProfileCredentialsProvider()).withEncryptionMaterials(materialProvider)
					.withCryptoConfiguration(cryptoConfig).withRegion(clientRegion).build();

			// Download the object
			S3Object obj = encryptionClient.getObject(bucketName, keyName);
			S3ObjectInputStream os = obj.getObjectContent();

			OutputStream output = new FileOutputStream(file);
			IOUtils.copy(os, output);

			encryptionClient.shutdown();
		} catch (AmazonServiceException e) {
			// The call was transmitted successfully, but Amazon S3 couldn't process
			// it, so it returned an error response.
			e.printStackTrace();
		} catch (SdkClientException e) {
			// Amazon S3 couldn't be contacted for a response, or the client
			// couldn't parse the response from Amazon S3.
			e.printStackTrace();
		}
	}
}
