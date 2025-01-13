package api.img.get;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;

public class utilS3Grabber{

    private static final String BUCKET_NAME = "hhdbstorage";
    private static final Region REGION = Region.EU_WEST_2;
    private static final String ACCESS_KEY = "AKIA54WIGLXL5TLKTH4D";
    private static final String SECRET_KEY = "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";

    public static void fetchFileFromS3(ResultSet rs, HttpServletResponse resp, HttpServletRequest req) throws Exception {
        String s3Key = rs.getString("image_path");

        // Create an S3 client with the configured region and credentials
        S3Client s3Client = S3Client.builder()
                .region(REGION)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(ACCESS_KEY, SECRET_KEY)))
                .build();

        // Build a request to fetch the object from the specified bucket and key
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(s3Key)
                .build();

        // Stream the S3 object to the HTTP response output
        try (InputStream s3InputStream = s3Client.getObject(getObjectRequest); // Input stream from S3
             OutputStream os = resp.getOutputStream()) { // Output stream to client

            // Set the content type of the response based on the file's MIME type
            resp.setContentType(req.getServletContext().getMimeType(s3Key));

            // Buffer for reading and writing data in chunks
            byte[] buffer = new byte[1024];
            int bytesRead;

            // Read from S3 input stream and write to HTTP response output stream
            while ((bytesRead = s3InputStream.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

    }
}
