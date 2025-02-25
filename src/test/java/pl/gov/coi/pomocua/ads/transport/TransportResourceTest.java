package pl.gov.coi.pomocua.ads.transport;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.CrudRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import pl.gov.coi.pomocua.ads.BaseResourceTest;
import pl.gov.coi.pomocua.ads.Location;
import pl.gov.coi.pomocua.ads.OffersVM;
import pl.gov.coi.pomocua.ads.UserId;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.gov.coi.pomocua.ads.transport.TransportTestDataGenerator.TRANSPORT_DATE;
import static pl.gov.coi.pomocua.ads.transport.TransportTestDataGenerator.aTransportOffer;

class TransportResourceTest extends BaseResourceTest<TransportOffer, TransportOfferVM> {

    @Autowired
    private TransportOfferRepository repository;

    @Override
    protected Class<TransportOfferVM> getClazz() {
        return TransportOfferVM.class;
    }

    @Override
    protected String getOfferSuffix() {
        return "transport";
    }

    @Override
    protected TransportOfferVM sampleOfferRequest() {
        return aTransportOffer().build();
    }

    @Override
    protected CrudRepository<TransportOffer, Long> getRepository() {
        return repository;
    }

    @Nested
    class Searching {
        @Test
        void shouldFindByOrigin() {
            TransportOfferVM transportOffer1 = postOffer(aTransportOffer()
                    .origin(new Location("mazowieckie", "warszawa"))
                    .build());
            postOffer(aTransportOffer()
                    .origin(new Location("Pomorskie", "Wejherowo"))
                    .build());
            postOffer(aTransportOffer()
                    .origin(new Location("Wielkopolskie", "Warszawa"))
                    .build());

            TransportOfferSearchCriteria searchCriteria = new TransportOfferSearchCriteria();
            searchCriteria.setOrigin(new Location("Mazowieckie", "Warszawa"));
            var results = searchOffers(searchCriteria);

            assertThat(results).hasSize(1);
            assertThat(results).first().isEqualTo(transportOffer1);
        }

        @Test
        void shouldFindByOriginWithNullOrigin() {
            TransportOfferVM transportOffer1 = postOffer(aTransportOffer()
                    .origin(new Location("mazowieckie", "warszawa"))
                    .build());
            TransportOfferVM transportOffer2 = postOffer(aTransportOffer()
                    .origin(null)
                    .build());
            postOffer(aTransportOffer()
                    .origin(new Location("Pomorskie", "Wejherowo"))
                    .build());
            postOffer(aTransportOffer()
                    .origin(new Location("Wielkopolskie", "Warszawa"))
                    .build());

            TransportOfferSearchCriteria searchCriteria = new TransportOfferSearchCriteria();
            searchCriteria.setOrigin(new Location("Mazowieckie", "Warszawa"));
            var results = searchOffers(searchCriteria);

            assertThat(results).hasSize(2);
            assertThat(results).contains(transportOffer1, transportOffer2);
        }

        @Test
        void shouldFindByDestination() {
            TransportOfferVM transportOffer1 = postOffer(aTransportOffer()
                    .destination(new Location("pomorskie", "GdyniA"))
                    .build());
            postOffer(aTransportOffer()
                    .destination(new Location("Pomorskie", "Wejherowo"))
                    .build());

            TransportOfferSearchCriteria searchCriteria = new TransportOfferSearchCriteria();
            searchCriteria.setDestination(new Location("Pomorskie", "Gdynia"));
            var results = searchOffers(searchCriteria);

            assertThat(results).hasSize(1);
            assertThat(results).first().isEqualTo(transportOffer1);
        }

        @Test
        void shouldFindByDestinationWithNullDestination() {
            TransportOfferVM transportOffer1 = postOffer(aTransportOffer()
                    .destination(new Location("pomorskie", "GdyniA"))
                    .build());
            TransportOfferVM transportOffer2 = postOffer(aTransportOffer()
                    .destination(null)
                    .build());
            postOffer(aTransportOffer()
                    .destination(new Location("Pomorskie", "Wejherowo"))
                    .build());

            TransportOfferSearchCriteria searchCriteria = new TransportOfferSearchCriteria();
            searchCriteria.setDestination(new Location("Pomorskie", "Gdynia"));
            var results = searchOffers(searchCriteria);

            assertThat(results).hasSize(2);
            assertThat(results).contains(transportOffer1, transportOffer2);
        }

        @Test
        void shouldFindByCapacity() {
            TransportOfferVM transportOffer1 = postOffer(aTransportOffer()
                    .capacity(10)
                    .build());
            TransportOfferVM transportOffer2 = postOffer(aTransportOffer()
                    .capacity(11)
                    .build());
            postOffer(aTransportOffer()
                    .capacity(1)
                    .build());

            TransportOfferSearchCriteria searchCriteria = new TransportOfferSearchCriteria();
            searchCriteria.setCapacity(10);
            var results = searchOffers(searchCriteria);
            assertThat(results).containsExactly(transportOffer1,transportOffer2);
        }

        @Test
        void shouldFindByTransportDate() {
            LocalDate transportDate1 = LocalDate.now().plusDays(2L);
            LocalDate transportDate2 = LocalDate.now().plusDays(3L);

            TransportOfferVM transportOffer1 = postOffer(aTransportOffer()
                    .transportDate(transportDate1)
                    .build());
            postOffer(aTransportOffer()
                    .transportDate(transportDate2)
                    .build());

            TransportOfferSearchCriteria searchCriteria = new TransportOfferSearchCriteria();
            searchCriteria.setTransportDate(transportDate1);
            var results = searchOffers(searchCriteria);
            assertThat(results).containsExactly(transportOffer1);
        }

        @Test
        void shouldReturnPageOfData() {
            postOffer(aTransportOffer().title("a").build());
            postOffer(aTransportOffer().title("b").build());
            postOffer(aTransportOffer().title("c").build());
            postOffer(aTransportOffer().title("d").build());
            postOffer(aTransportOffer().title("e").build());
            postOffer(aTransportOffer().title("f").build());

            PageRequest page = PageRequest.of(1, 2);
            var results = searchOffers(page);

            assertThat(results.totalElements).isEqualTo(6);
            assertThat(results.content)
                    .hasSize(2)
                    .extracting(r -> r.getTitle())
                    .containsExactly("c", "d");
        }

        @Test
        @Disabled("Embedded Postgres cannot provide expected sorting order")
        void shouldSortResults() {
            postOffer(aTransportOffer().title("a").build());
            postOffer(aTransportOffer().title("bb").build());
            postOffer(aTransportOffer().title("bą").build());
            postOffer(aTransportOffer().title("c").build());
            postOffer(aTransportOffer().title("ć").build());
            postOffer(aTransportOffer().title("d").build());

            PageRequest page = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "title"));
            var results = searchOffers(page);

            assertThat(results.content)
                    .extracting(r -> r.getTitle())
                    .containsExactly("d", "ć", "c", "bb", "bą", "a");
        }

        @Test
        void shouldIgnoreDeactivatedOffer() {
            TransportOfferVM offer = postOffer(aTransportOffer().build());
            deleteOffer(offer.getId());

            PageRequest page = PageRequest.of(0, 10);
            var results = searchOffers(page);

            assertThat(results.totalElements).isEqualTo(0);
            assertThat(results.content).isEmpty();
        }

        @Test
        void shouldIgnoreCaseWhenFindByOrigin() {
            TransportOfferVM transportOffer1 = postOffer(aTransportOffer()
                .origin(new Location("mazowieckie", "warszawa"))
                .build());
            postOffer(aTransportOffer()
                .origin(new Location("Pomorskie", "Wejherowo"))
                .build());
            postOffer(aTransportOffer()
                .origin(new Location("Wielkopolskie", "Warszawa"))
                .build());

            TransportOfferSearchCriteria searchCriteria = new TransportOfferSearchCriteria();
            searchCriteria.setOrigin(new Location("Mazowieckie", "WarszAWA"));
            var results = searchOffers(searchCriteria);

            assertThat(results).hasSize(1);
            assertThat(results).first().isEqualTo(transportOffer1);
        }

        @Test
        void shouldIgnoreCaseWhenFindByDestination() {
            TransportOfferVM transportOffer1 = postOffer(aTransportOffer()
                .destination(new Location("pomorskie", "GdyniA"))
                .build());
            postOffer(aTransportOffer()
                .destination(new Location("Pomorskie", "Wejherowo"))
                .build());

            TransportOfferSearchCriteria searchCriteria = new TransportOfferSearchCriteria();
            searchCriteria.setDestination(new Location("Pomorskie", "GDYnia"));
            var results = searchOffers(searchCriteria);

            assertThat(results).hasSize(1);
            assertThat(results).first().isEqualTo(transportOffer1);
        }
    }

    @Nested
    class ValidationTest {

        @Test
        void shouldRejectNullCapacity() {
            TransportOfferVM offer = sampleOfferRequest();
            offer.setCapacity(null);
            assertPostResponseStatus(offer, HttpStatus.BAD_REQUEST);
        }

        @ParameterizedTest
        @ValueSource(ints = {-10, -1, 0, 100, 101, 1000})
        void shouldRejectIncorrectCapacity(int capacity) {
            TransportOfferVM offer = sampleOfferRequest();
            offer.setCapacity(capacity);
            assertPostResponseStatus(offer, HttpStatus.BAD_REQUEST);
        }

        @Test
        void shouldAcceptNullOrigin() {
            TransportOfferVM offer = sampleOfferRequest();
            offer.setOrigin(null);
            assertPostResponseStatus(offer, HttpStatus.CREATED);
        }

        @Test
        void shouldAcceptNullDestination() {
            TransportOfferVM offer = sampleOfferRequest();
            offer.setDestination(null);
            assertPostResponseStatus(offer, HttpStatus.CREATED);
        }
    }

    @Nested
    class Updating {
        @Test
        void shouldUpdateOffer() {
            TransportOfferVM offer = postSampleOffer();
            var updateJson = TransportTestDataGenerator.sampleUpdateJson();

            ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            TransportOffer updatedOffer = getOfferFromRepository(offer.getId());
            assertThat(updatedOffer.title).isEqualTo("new title");
            assertThat(updatedOffer.description).isEqualTo("new description");
            assertThat(updatedOffer.origin.region).isEqualTo("dolnośląskie");
            assertThat(updatedOffer.origin.city).isEqualTo("Wrocław");
            assertThat(updatedOffer.destination.region).isEqualTo("podlaskie");
            assertThat(updatedOffer.destination.city).isEqualTo("Białystok");
            assertThat(updatedOffer.capacity).isEqualTo(35);
            assertThat(updatedOffer.transportDate).isEqualTo(TRANSPORT_DATE);
        }

        @Test
        public void shouldUpdateModifiedDate() {
            testTimeProvider.setCurrentTime(Instant.parse("2022-03-07T15:23:22Z"));
            TransportOfferVM offer = postSampleOffer();
            var updateJson = TransportTestDataGenerator.sampleUpdateJson();

            testTimeProvider.setCurrentTime(Instant.parse("2022-04-01T12:00:00Z"));
            ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
            TransportOffer updatedOffer = getOfferFromRepository(offer.getId());
            assertThat(updatedOffer.modifiedDate).isEqualTo(Instant.parse("2022-04-01T12:00:00Z"));
        }

        @Test
        void shouldReturn404WhenOfferDoesNotExist() {
            var updateJson = TransportTestDataGenerator.sampleUpdateJson();

            ResponseEntity<Void> response = updateOffer(123L, updateJson);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldReturn404WhenOfferDeactivated() {
            TransportOfferVM offer = postSampleOffer();
            deleteOffer(offer.getId());
            var updateJson = TransportTestDataGenerator.sampleUpdateJson();

            ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        void shouldReturn404WhenOfferDoesNotBelongToCurrentUser() {
            testUser.setCurrentUserWithId(new UserId("other-user-2"));
            TransportOfferVM offer = postSampleOffer();
            var updateJson = TransportTestDataGenerator.sampleUpdateJson();

            testUser.setCurrentUserWithId(new UserId("current-user-1"));
            ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Nested
        class Validation {
            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {"<", ">", "(", ")", "%", "@", "\"", "'"})
            void shouldRejectMissingOrInvalidTitle(String title) {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.title = title;

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                TransportOffer notUpdatedOffer = getOfferFromRepository(offer.getId());
                assertThat(notUpdatedOffer.title).isEqualTo(offer.getTitle());
            }

            @Test
            void shouldRejectTooLongTitle() {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.title = "a".repeat(81);

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                TransportOffer notUpdatedOffer = getOfferFromRepository(offer.getId());
                assertThat(notUpdatedOffer.title).isEqualTo(offer.getTitle());
            }

            @ParameterizedTest
            @NullAndEmptySource
            @ValueSource(strings = {"<", ">", "%", "\"", "'"})
            void shouldRejectMissingOrInvalidDescription(String description) {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.description = description;

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                TransportOffer notUpdatedOffer = getOfferFromRepository(offer.getId());
                assertThat(notUpdatedOffer.description).isEqualTo(offer.getDescription());
            }

            @Test
            void shouldRejectTooLongDescription() {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.description = "a".repeat(2001);

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                TransportOffer notUpdatedOffer = getOfferFromRepository(offer.getId());
                assertThat(notUpdatedOffer.description).isEqualTo(offer.getDescription());
            }

            @ParameterizedTest
            @MethodSource("pl.gov.coi.pomocua.ads.transport.TransportResourceTest#invalidLocations")
            void shouldRejectInvalidOrigin(Location location) {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.origin = location;

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                TransportOffer notUpdatedOffer = getOfferFromRepository(offer.getId());
                assertThat(notUpdatedOffer.origin).isEqualTo(offer.getOrigin());
            }

            @ParameterizedTest
            @NullSource
            void shouldAcceptMissingOrigin(Location location) {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.origin = location;

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                TransportOffer updatedOffer = getOfferFromRepository(offer.getId());
                assertThat(updatedOffer.origin).isNull();
            }

            @ParameterizedTest
            @MethodSource("pl.gov.coi.pomocua.ads.transport.TransportResourceTest#invalidLocations")
            void shouldRejectInvalidDestination(Location location) {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.destination = location;

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                TransportOffer notUpdatedOffer = getOfferFromRepository(offer.getId());
                assertThat(notUpdatedOffer.destination).isEqualTo(offer.getDestination());
            }

            @ParameterizedTest
            @NullSource
            void shouldAcceptMissingDestination(Location location) {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.destination = location;

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                TransportOffer updatedOffer = getOfferFromRepository(offer.getId());
                assertThat(updatedOffer.destination).isNull();
            }

            @ParameterizedTest
            @NullSource
            @ValueSource(ints = {-1, 0, 100, 101})
            void shouldRejectMissingOrInvalidCapacity(Integer capacity) {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.capacity = capacity;

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                TransportOffer notUpdatedOffer = getOfferFromRepository(offer.getId());
                assertThat(notUpdatedOffer.capacity).isEqualTo(offer.getCapacity());
            }

            @Test
            void shouldAcceptMissingTransportDate() {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.transportDate = null;

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                TransportOffer updatedOffer = getOfferFromRepository(offer.getId());
                assertThat(updatedOffer.transportDate).isNull();
            }

            @ParameterizedTest
            @ValueSource(strings = {"invalid phone", "+48 invalid phone", "0048123", "+48 123 123", "+48 123", "+48000000000", "0048123456"})
            void shouldRejectInvalidPhoneNumber(String invalidPhoneNumber) {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.phoneNumber = invalidPhoneNumber;

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            }

            @Test
            void shouldAcceptMissingPhoneNumber() {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.phoneNumber = null;

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                TransportOffer updatedOffer = getOfferFromRepository(offer.getId());
                assertThat(updatedOffer.phoneNumber).isNull();
            }

            @ParameterizedTest
            @ValueSource(strings = {"+48123456789", "+48 123 456 789", "+48 123-456-789", "0048 123456789", "0048 (123) 456-789"})
            void shouldAcceptPhoneNumberInVariousFormatsAndNormalizeIt(String phoneNumber) {
                TransportOfferVM offer = postSampleOffer();
                var updateJson = TransportTestDataGenerator.sampleUpdateJson();
                updateJson.phoneNumber = phoneNumber;

                ResponseEntity<Void> response = updateOffer(offer.getId(), updateJson);

                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
                TransportOffer updatedOffer = getOfferFromRepository(offer.getId());
                assertThat(updatedOffer.phoneNumber).isEqualTo("123456789");
                assertThat(updatedOffer.phoneCountryCode).isEqualTo("48");
            }
        }
    }

    static Stream<Location> invalidLocations() {
        return Stream.of(
                new Location(null, "city"),
                new Location("   ", "city"),
                new Location("region", null),
                new Location("region", "   ")
        );
    }

    private List<TransportOfferVM> searchOffers(TransportOfferSearchCriteria searchCriteria) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/" + getOfferSuffix());
        if (searchCriteria.getOrigin() != null) {
            builder
                    .queryParam("origin.city", searchCriteria.getOrigin().getCity())
                    .queryParam("origin.region", searchCriteria.getOrigin().getRegion());
        }
        if (searchCriteria.getDestination() != null) {
            builder
                    .queryParam("destination.city", searchCriteria.getDestination().getCity())
                    .queryParam("destination.region", searchCriteria.getDestination().getRegion());
        }
        if (searchCriteria.getCapacity() != null) {
            builder.queryParam("capacity", searchCriteria.getCapacity());
        }
        if (searchCriteria.getTransportDate() != null) {
            builder.queryParam("transportDate", searchCriteria.getTransportDate());
        }
        String url = builder.encode().toUriString();

        return listOffers(URI.create(url)).content;
    }

    private OffersVM<TransportOfferVM> searchOffers(PageRequest pageRequest) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/api/" + getOfferSuffix());
        builder.queryParam("page", pageRequest.getPageNumber());
        builder.queryParam("size", pageRequest.getPageSize());
        pageRequest.getSort().forEach(sort -> {
            builder.queryParam("sort", "%s,%s".formatted(sort.getProperty(), sort.getDirection()));
        });
        String url = builder.encode().toUriString();

        return listOffers(URI.create(url));
    }
}
