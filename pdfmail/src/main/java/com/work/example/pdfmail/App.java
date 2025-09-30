package com.work.example.pdfmail;

import java.io.File;
import java.io.FileOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

import com.itextpdf.text.*;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.*;

// --- Employee Base Class ---
class Employee2 {
    int empId;
    String empName, empEmail, empDesignation;

    public Employee2(int empId, String empName, String empEmail, String empDesignation) {
        this.empId = empId;
        this.empName = empName;
        this.empEmail = empEmail;
        this.empDesignation = empDesignation;
    }
}

// --- SalaryDetails Subclass ---
class SalaryDetails2 extends Employee2 {
    double empSalary, empAllowance;
    double empTotalSalary;

    SalaryDetails2(int empId, String empName, String empEmail, String empDesignation, double empSalary) {
        super(empId, empName, empEmail, empDesignation);
        this.empSalary = empSalary;
    }

    void report() {
        if (empDesignation.equalsIgnoreCase("Manager")) empAllowance = 0.18;
        else if (empDesignation.equalsIgnoreCase("Sales")) empAllowance = 0.16;
        else if (empDesignation.equalsIgnoreCase("Developer")) empAllowance = 0.13;
        else if (empDesignation.equalsIgnoreCase("Executive")) empAllowance = 0.21;
        else empAllowance = 0.09;

        empTotalSalary = empSalary + (empSalary * empAllowance);
    }
}

// --- Main Class ---
public class App {

    // ✅ Create Human Logo
    public static String createLogo() {
        try {
            int width = 150, height = 150;
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = img.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Transparent background
            g2d.setColor(new Color(255, 255, 255, 0));
            g2d.fillRect(0, 0, width, height);

            // Head
            g2d.setColor(Color.BLUE);
            g2d.fillOval(55, 10, 40, 40);

            // Body
            g2d.setStroke(new BasicStroke(6));
            g2d.drawLine(75, 50, 75, 100);

            // Arms
            g2d.drawLine(75, 60, 45, 85);
            g2d.drawLine(75, 60, 105, 85);

            // Legs
            g2d.drawLine(75, 100, 50, 130);
            g2d.drawLine(75, 100, 100, 130);

            g2d.dispose();
            File file = new File("human_logo.png");
            ImageIO.write(img, "png", file);
            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ✅ Send Email with attachment
    public static void sendEmail(String toEmail, String subject, String body, String attachmentPath) {
        final String fromEmail = "rizwanmohamed042@gmail.com"; // sender
        final String password = "uznu yynw dptv yaol";      // sender app password

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject(subject);

            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            if (attachmentPath != null) {
                MimeBodyPart attachPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachmentPath);
                attachPart.setDataHandler(new DataHandler(source));
                attachPart.setFileName(new File(attachmentPath).getName());
                multipart.addBodyPart(attachPart);
            }

            message.setContent(multipart);
            Transport.send(message);
            System.out.println("✅ Email sent to: " + toEmail);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter number of employees: ");
        int n = sc.nextInt();
        SalaryDetails2[] employees = new SalaryDetails2[n];

        for (int i = 0; i < n; i++) {
            System.out.println("\n--- Employee " + (i + 1) + " ---");
            System.out.print("ID: "); int id = sc.nextInt();
            System.out.print("Name: "); String name = sc.next();
            System.out.print("Email: "); String email = sc.next();
            System.out.print("Designation: "); String desig = sc.next();
            System.out.print("Salary: "); double sal = sc.nextDouble();
            employees[i] = new SalaryDetails2(id, name, email, desig, sal);
            employees[i].report();
        }
        sc.close();

        String logoPath = createLogo();

        // Generate individual PDFs and send emails
        for (SalaryDetails2 emp : employees) {
            try {
                String pdfFile = "Employee_" + emp.empId + ".pdf";
                Document doc = new Document(PageSize.A4);
                PdfWriter.getInstance(doc, new FileOutputStream(pdfFile));
                doc.open();

                // Logo
                if (logoPath != null) {
                    com.itextpdf.text.Image logo = com.itextpdf.text.Image.getInstance(logoPath);
                    logo.scaleAbsolute(120, 120);
                    logo.setAlignment(Element.ALIGN_CENTER);
                    doc.add(logo);
                }

                // Title
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD, BaseColor.BLUE);
                Paragraph title = new Paragraph("\nEMPLOYEE SALARY REPORT\n\n", titleFont);
                title.setAlignment(Element.ALIGN_CENTER);
                doc.add(title);

                // Table
                PdfPTable table = new PdfPTable(7);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{1f,2f,3f,2f,2f,2f,2f});

                Font headFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, BaseColor.WHITE);
                String[] headers = {"ID","Name","Email","Designation","Salary","Allowance","Total Salary"};
                for(String h : headers) {
                    PdfPCell cell = new PdfPCell(new Phrase(h, headFont));
                    cell.setBackgroundColor(BaseColor.DARK_GRAY);
                    cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                    table.addCell(cell);
                }

                Font dataFont = new Font(Font.FontFamily.TIMES_ROMAN, 11, Font.NORMAL, BaseColor.BLACK);
                table.addCell(new PdfPCell(new Phrase(String.valueOf(emp.empId), dataFont)));
                table.addCell(new PdfPCell(new Phrase(emp.empName, dataFont)));
                table.addCell(new PdfPCell(new Phrase(emp.empEmail, dataFont)));
                table.addCell(new PdfPCell(new Phrase(emp.empDesignation, dataFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", emp.empSalary), dataFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", emp.empAllowance*100) + "%", dataFont)));
                table.addCell(new PdfPCell(new Phrase(String.format("%.2f", emp.empTotalSalary), dataFont)));

                doc.add(table);
                doc.close();

                // Send email
                sendEmail(emp.empEmail,
                        "Your Salary Report",
                        "Dear " + emp.empName + ",\n\nPlease find your salary report attached.\n\nRegards,\nHR Department",
                        pdfFile);

                // Delete PDF after sending (optional)
                new File(pdfFile).delete();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
