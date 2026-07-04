// Snippet source for kensa.dev/docs/api/fixtures.md — Java examples
package apidocs;

import dev.kensa.fixture.Fixture;
import dev.kensa.fixture.ParameterFixture;
import dev.kensa.fixture.Parents;
import dev.kensa.fixture.SecondaryFixture;

import java.time.LocalDate;

import static dev.kensa.fixture.FixtureKt.createFixture;
import static dev.kensa.fixture.FixtureKt.createParameterFixture;

class FixturesSnippets {

    record AppointmentSlot(LocalDate date, String slot) {}
    record ServiceAddress(String postcode, String line1, String town, String county) {}
    record TrackingId() {}

    static final Fixture<LocalDate> appointmentDate = createFixture("Appointment Date", LocalDate::now);
    static final Fixture<String> appointmentTimeSlot = createFixture("Appointment Time Slot", () -> "AM");
    static final Fixture<String> postcode = createFixture("Postcode", () -> "SW1A 1AA");
    static final Fixture<String> addressLine1 = createFixture("Address Line 1", () -> "1 Main St");
    static final Fixture<String> town = createFixture("Town", () -> "Springfield");
    static final Fixture<String> county = createFixture("County", () -> "Westshire");

    // 2 parents
    static final Fixture<AppointmentSlot> slot =
            createFixture("Appointment Slot", appointmentDate, appointmentTimeSlot,
                    (date, timeSlot) -> new AppointmentSlot(date, timeSlot));

    // more than 3 dependencies — construct a SecondaryFixture directly
    static final Fixture<ServiceAddress> serviceAddress = new SecondaryFixture<ServiceAddress>(
            "Service Address",
            fixtures -> new ServiceAddress(
                    fixtures.get(postcode), fixtures.get(addressLine1),
                    fixtures.get(town), fixtures.get(county)
            ),
            new Parents.Three<>(postcode, addressLine1, town),
            false
    );

    // highlighted
    static final Fixture<TrackingId> trackingId = createFixture("Tracking Id", /* highlighted = */ true, TrackingId::new);

    // parameter-derived
    static final ParameterFixture<String> GREETING =
            createParameterFixture("Greeting", "userName", (String name) -> "Hello, " + name);
}
