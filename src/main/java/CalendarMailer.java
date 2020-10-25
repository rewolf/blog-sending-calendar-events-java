import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.parameter.ParticipationLevel;
import biweekly.property.Attendee;
import biweekly.property.Method;
import biweekly.property.Organizer;
import biweekly.util.Duration;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.util.Date;
import java.util.Properties;

public class CalendarMailer {
    public static void main(String[] args) throws Exception {
        // Create a mail session, specifying SMTP server
        final Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", "your.smtp.domain");
        final Session mailSession = Session.getDefaultInstance(properties);

        // Calendar as message content
        final MimeMessage message = createMultipartTextFileAndCalendar(mailSession);
        // Simple Calendar-only mail:
//        final MimeMessage message = createICalPart(new MimeMessage(mailSession));

        // send
        final InternetAddress toAddress = new InternetAddress("not.amazing.attendee@domain", "Not Amazing Attendee");
        final InternetAddress fromAddress = new InternetAddress("amazing.organizer@domain", "Amazing Organizer");
        message.setRecipient(Message.RecipientType.TO, toAddress);
        message.setSender(fromAddress);

        Transport.send(message);
    }

    private static MimeMessage createMultipartTextFileAndCalendar(final Session mailSession) throws Exception {
        final MimeMessage message = new MimeMessage(mailSession);
        final MimeMultipart mixedMultipart = new MimeMultipart("mixed");
        mixedMultipart.addBodyPart(createICalPart(new MimeBodyPart()));
        mixedMultipart.addBodyPart(createTextAttachment());
        message.setContent(mixedMultipart);
        return message;
    }

    public static MimeBodyPart createTextAttachment() throws Exception {
        final MimeBodyPart textAttachmentPart =  new MimeBodyPart();
        textAttachmentPart.setContent("no stuffs", "text/plain; charset=UTF-8");
        textAttachmentPart.addHeader("Content-Disposition", "attachment; filename=fun.txt");
        return textAttachmentPart;
    }

    public static <T extends MimePart> T createICalPart(T mimePartForCalendar) throws Exception {
        final DataSource source = new ByteArrayDataSource(generateICalData(), "text/calendar; charset=UTF-8");
        mimePartForCalendar.setDataHandler(new DataHandler(source));
        mimePartForCalendar.setHeader("Content-Type", "text/calendar; charset=UTF-8; method=REQUEST");
        return mimePartForCalendar;
    }


    public static String generateICalData() {
        ICalendar ical = new ICalendar();
        ical.addProperty(new Method(Method.REQUEST));

            VEvent event = new VEvent();
                event.setSummary("Programming Hotdogs 101");
                event.setDescription("You having been invited to this amazing event! Let's program Hotdogs!");

                event.setDateStart(new Date());
                event.setDuration(new Duration.Builder()
                                          .hours(1)
                                          .build());

                event.setOrganizer(new Organizer("Amazing Organizer", "amazing.organizer@domain"));

                Attendee a = new Attendee("Not Amazing Attendee", "not.amazing.attendee@domain");
                a.setParticipationLevel(ParticipationLevel.REQUIRED);
                event.addAttendee(a);
            ical.addEvent(event);

        return Biweekly.write(ical).go();
    }
}
