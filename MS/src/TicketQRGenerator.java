import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;

public class TicketQRGenerator {

    public static void generateTicketQR(String data, String filePath) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, 300, 300);

            Path path = FileSystems.getDefault().getPath(filePath);
            MatrixToImageWriter.writeToPath(bitMatrix, "PNG", path);

            System.out.println("QR Code generated successfully: " + filePath);

        } catch (WriterException | IOException e) {
            System.out.println("Error generating QR Code: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        // Example data for testing
        String ticketInfo = "Ticket ID: 12345\nFrom: Chennai Central\nTo: Airport\nFare: ₹30";
        generateTicketQR(ticketInfo, "ticket123.png");
    }
}

