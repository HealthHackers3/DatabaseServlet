import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.nio.file.Path;

public class awscheck {
    public static void main(String[] args) {
        try {
            // Replace with your keys
            String accessKey = "AKIA54WIGLXL5TLKTH4D";
            String secretKey = "79vfbJSiPydqbp21fK6a8TOuDOXcgbad6EA5Y3tJ";

            // AWS Region and S3 Bucket Name
            Region region = Region.EU_WEST_2; // Replace with your S3 region
            String bucketName = "hhdbstorage";

            // Create an S3 client
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKey, secretKey);
            S3Client s3Client = S3Client.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .region(region)
                    .build();

            // Example: Upload a file
            String key = "uploads/sample-file.jpg"; // S3 object key
            Path filePath = Path.of("C:\\Users\\sctcl\\IdeaProjects\\HHDatabase\\var\\uploads\\fullResPostImages\\eletronmicr.jpeg");

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.putObject(putObjectRequest, filePath);
            System.out.println("File uploaded successfully!");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
