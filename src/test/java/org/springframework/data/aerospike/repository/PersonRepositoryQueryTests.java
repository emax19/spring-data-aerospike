package org.springframework.data.aerospike.repository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.aerospike.BaseBlockingIntegrationTests;
import org.springframework.data.aerospike.IndexUtils;
import org.springframework.data.aerospike.repository.query.CriteriaDefinition;
import org.springframework.data.aerospike.sample.Address;
import org.springframework.data.aerospike.sample.Person;
import org.springframework.data.aerospike.sample.PersonRepository;
import org.springframework.data.aerospike.sample.PersonSomeFields;
import org.springframework.data.aerospike.utility.TestUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.data.aerospike.AsCollections.of;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersonRepositoryQueryTests extends BaseBlockingIntegrationTests {

    @Autowired
    PersonRepository<Person> repository;
    static final Person dave = Person.builder().id(nextId()).firstName("Dave").lastName("Matthews").age(42)
        .strings(List.of("str1", "str2")).address(new Address("Foo Street 1", 1, "C0123", "Bar"))
        .build();
    static final Person donny = Person.builder().id(nextId()).firstName("Donny").lastName("Macintire").age(39)
        .strings(List.of("str1", "str2", "str3")).stringMap(of("key1", "val1")).build();
    static final Person oliver = Person.builder().id(nextId()).firstName("Oliver August").lastName("Matthews").age(14)
        .ints(List.of(425, 550, 990)).build();
    static final Person alicia = Person.builder().id(nextId()).firstName("Alicia").lastName("Keys").age(30)
        .ints(List.of(550, 600, 990)).build();
    static final Person carter = Person.builder().id(nextId()).firstName("Carter").lastName("Beauford").age(49)
        .intMap(of("key1", 0, "key2", 1))
        .address(new Address("Foo Street 2", 2, "C0124", "C0123")).build();
    static final Person boyd = Person.builder().id(nextId()).firstName("Boyd").lastName("Tinsley").age(45)
        .stringMap(of("key1", "val1", "key2", "val2")).address(new Address(null, null, null, null))
        .build();
    static final Person stefan = Person.builder().id(nextId()).firstName("Stefan").lastName("Lessard").age(34).build();
    static final Person leroi = Person.builder().id(nextId()).firstName("Leroi").lastName("Moore").age(44).build();
    static final Person leroi2 = Person.builder().id(nextId()).firstName("Leroi").lastName("Moore").age(25).build();
    static final Person matias = Person.builder().id(nextId()).firstName("Matias").lastName("Craft").age(24).build();
    static final Person douglas = Person.builder().id(nextId()).firstName("Douglas").lastName("Ford").age(25).build();
    public static final List<Person> allPersons = List.of(dave, donny, oliver, alicia, carter, boyd, stefan,
        leroi, leroi2, matias, douglas);

    @BeforeAll
    public void beforeAll() {
        indexRefresher.refreshIndexes();
        repository.deleteAll(allPersons);
        repository.saveAll(allPersons);
    }

    @AfterAll
    public void afterAll() {
        repository.deleteAll(allPersons);
    }

    @Test
    void findByListContainingString_forExistingResult() {
        assertThat(repository.findByStringsContaining("str1")).containsOnly(dave, donny);
        assertThat(repository.findByStringsContaining("str2")).containsOnly(dave, donny);
        assertThat(repository.findByStringsContaining("str3")).containsOnly(donny);
    }

    @Test
    void findByListContainingString_forEmptyResult() {
        List<Person> persons = repository.findByStringsContaining("str5");
        assertThat(persons).isEmpty();
    }

    @Test
    void findByListContainingInteger_forExistingResult() {
        assertThat(repository.findByIntsContaining(550)).containsOnly(oliver, alicia);
        assertThat(repository.findByIntsContaining(990)).containsOnly(oliver, alicia);
        assertThat(repository.findByIntsContaining(600)).containsOnly(alicia);
    }

    @Test
    void findByListContainingInteger_forEmptyResult() {
        List<Person> persons = repository.findByIntsContaining(7777);

        assertThat(persons).isEmpty();
    }

    @Test
    void findByActiveTrue() {
        boolean initialState = dave.isActive();
        if (!initialState) {
            dave.setActive(true);
            repository.save(dave);
        }
        List<Person> persons = repository.findPersonsByActive(true);
        assertThat(persons).contains(dave);

        if (!initialState) {
            dave.setActive(false);
            repository.save(dave);
        }
    }

    @Test
    void findByListValueGreaterThan() {
        List<Person> persons = repository.findByIntsGreaterThan(549);

        assertThat(persons).containsOnly(oliver, alicia);
    }

    @Test
    void findByListValueLessThanOrEqual() {
        List<Person> persons = repository.findByIntsLessThanEqual(500);

        assertThat(persons).containsOnly(oliver);
    }

    @Test
    void findByListValueInRange() {
        List<Person> persons = repository.findByIntsBetween(500, 600);

        assertThat(persons).containsExactlyInAnyOrder(oliver, alicia);
    }

    @Test
    void findByMapKeysContaining() {
        assertThat(donny.getStringMap()).containsKey("key1");
        assertThat(boyd.getStringMap()).containsKey("key1");

        List<Person> persons = repository.findByStringMapContaining("key1",
            CriteriaDefinition.AerospikeMapCriteria.KEY);

        assertThat(persons).contains(donny, boyd);
    }

    @Test
    void findByMapValuesContaining() {
        assertThat(donny.getStringMap()).containsValue("val1");
        assertThat(boyd.getStringMap()).containsValue("val1");

        List<Person> persons = repository.findByStringMapContaining("val1",
            CriteriaDefinition.AerospikeMapCriteria.VALUE);

        assertThat(persons).contains(donny, boyd);
    }

    @Test
    void findByMapKeyValueEquals() {
        assertThat(donny.getStringMap()).containsKey("key1");
        assertThat(donny.getStringMap()).containsValue("val1");
        assertThat(boyd.getStringMap()).containsKey("key1");
        assertThat(boyd.getStringMap()).containsValue("val1");

        List<Person> persons = repository.findByStringMapEquals("key1", "val1");
        assertThat(persons).containsExactlyInAnyOrder(donny, boyd);

        // another way to call the method
        List<Person> persons2 = repository.findByStringMap("key1", "val1");
        assertThat(persons2).containsExactlyInAnyOrder(donny, boyd);

    }

    @Test
    void findByMapKeyValueNotEqual() {
        assertThat(carter.getIntMap()).containsKey("key1");
        assertThat(!carter.getIntMap().containsValue(22)).isTrue();

        List<Person> persons = repository.findByIntMapIsNot("key1", 22);

        assertThat(persons).contains(carter);
    }

    @Test
    void findByMapKeyValueContains() {
        assertThat(donny.getStringMap()).containsKey("key1");
        assertThat(donny.getStringMap()).containsValue("val1");
        assertThat(boyd.getStringMap()).containsKey("key1");
        assertThat(boyd.getStringMap()).containsValue("val1");

        List<Person> persons = repository.findByStringMapContaining("key1", "al");

        assertThat(persons).contains(donny, boyd);
    }

    @Test
    void findByMapKeyValueStartsWith() {
        assertThat(donny.getStringMap()).containsKey("key1");
        assertThat(donny.getStringMap()).containsValue("val1");
        assertThat(boyd.getStringMap()).containsKey("key1");
        assertThat(boyd.getStringMap()).containsValue("val1");

        List<Person> persons = repository.findByStringMapStartsWith("key1", "val");

        assertThat(persons).contains(donny, boyd);
    }

    @Test
    void findByMapKeyValueLike() {
        assertThat(donny.getStringMap()).containsKey("key1");
        assertThat(donny.getStringMap()).containsValue("val1");
        assertThat(boyd.getStringMap()).containsKey("key1");
        assertThat(boyd.getStringMap()).containsValue("val1");

        List<Person> persons = repository.findByStringMapLike("key1", "^.*al1$");

        assertThat(persons).contains(donny, boyd);
    }

    @Test
    void findByMapKeyValueGreaterThan() {
        assertThat(carter.getIntMap()).containsKey("key2");
        assertThat(carter.getIntMap().get("key2") > 0).isTrue();

        List<Person> persons = repository.findByIntMapGreaterThan("key2", 0);

        assertThat(persons).containsExactly(carter);
    }

    @Test
    void findByMapKeyValueLessThanOrEqual() {
        assertThat(carter.getIntMap()).containsKey("key2");
        assertThat(carter.getIntMap().get("key2") > 0).isTrue();

        List<Person> persons = repository.findByIntMapLessThanEqual("key2", 1);

        assertThat(persons).containsExactly(carter);
    }

    @Test
    void findByMapKeyValueBetween() {
        assertThat(carter.getIntMap()).containsKey("key1");
        assertThat(carter.getIntMap()).containsKey("key2");
        assertThat(carter.getIntMap().get("key1") >= 0).isTrue();
        assertThat(carter.getIntMap().get("key2") >= 0).isTrue();

        List<Person> persons = repository.findByIntMapBetween("key2", 0, 1);

        assertThat(persons).contains(carter);
    }

    @Test
    void findByRegDateBefore() {
        dave.setRegDate(LocalDate.of(1980, 3, 10));
        repository.save(dave);

        List<Person> persons = repository.findByRegDateBefore(LocalDate.of(1981, 3, 10));
        assertThat(persons).contains(dave);

        dave.setDateOfBirth(null);
        repository.save(dave);
    }

    @Test
    void findByDateOfBirthAfter() {
        dave.setDateOfBirth(new Date());
        repository.save(dave);

        List<Person> persons = repository.findByDateOfBirthAfter(new Date(126230400));
        assertThat(persons).contains(dave);

        dave.setDateOfBirth(null);
        repository.save(dave);
    }

    @Test
    void findByRegDate() {
        LocalDate date = LocalDate.of(1970, 3, 10);
        carter.setRegDate(date);
        repository.save(carter);

        List<Person> persons = repository.findByRegDate(date);
        assertThat(persons).contains(carter);

        carter.setRegDate(null);
        repository.save(carter);
    }

    @Test
    void findByFirstNameContaining() {
        List<Person> persons = repository.findByFirstNameContaining("er");
        assertThat(persons).containsExactlyInAnyOrder(carter, oliver, leroi, leroi2);
    }

    @Test
    void findByFirstNameLike() { // with a wildcard
        List<Person> persons = repository.findByFirstNameLike("Ca.*er");
        assertThat(persons).contains(carter);

        List<Person> persons0 = repository.findByFirstNameLikeIgnoreCase("CART.*er");
        assertThat(persons0).contains(carter);

        List<Person> persons1 = repository.findByFirstNameLike(".*ve.*");
        assertThat(persons1).contains(dave, oliver);

        List<Person> persons2 = repository.findByFirstNameLike("Carr.*er");
        assertThat(persons2).isEmpty();
    }

    @Test
    void findByAddressZipCodeContaining() {
        carter.setAddress(new Address("Foo Street 2", 2, "C10124", "C0123"));
        repository.save(carter);
        dave.setAddress(new Address("Foo Street 1", 1, "C10123", "Bar"));
        repository.save(dave);
        boyd.setAddress(new Address(null, null, null, null));
        repository.save(boyd);

        List<Person> persons = repository.findByAddressZipCodeContaining("C10");

        assertThat(persons).containsExactlyInAnyOrder(carter, dave);
    }

    @Test
    public void findPersonById() {
        Optional<Person> person = repository.findById(dave.getId());

        assertThat(person).hasValueSatisfying(actual -> {
            assertThat(actual).isInstanceOf(Person.class);
            assertThat(actual).isEqualTo(dave);
        });
    }

    @Test
    public void findAll() {
        List<Person> result = (List<Person>) repository.findAll();
        assertThat(result).containsExactlyInAnyOrderElementsOf(allPersons);
    }

    @Test
    public void findAllWithGivenIds() {
        List<Person> result = (List<Person>) repository.findAllById(List.of(dave.getId(), boyd.getId()));

        assertThat(result)
            .hasSize(2)
            .contains(dave)
            .doesNotContain(oliver, carter, alicia);
    }

    @Test
    public void findPersonsByLastName() {
        List<Person> result = repository.findByLastName("Beauford");

        assertThat(result)
            .hasSize(1)
            .containsOnly(carter);
    }

    @Test
    public void findPersonsSomeFieldsByLastNameProjection() {
        List<PersonSomeFields> result = repository.findPersonSomeFieldsByLastName("Beauford");

        assertThat(result)
            .hasSize(1)
            .containsOnly(carter.toPersonSomeFields());
    }

    @Test
    public void findDynamicTypeByLastNameDynamicProjection() {
        List<PersonSomeFields> result = repository.findByLastName("Beauford", PersonSomeFields.class);

        assertThat(result)
            .hasSize(1)
            .containsOnly(carter.toPersonSomeFields());
    }

    @Test
    public void findPersonsByFriendAge() {
        oliver.setFriend(alicia);
        repository.save(oliver);
        dave.setFriend(oliver);
        repository.save(dave);
        carter.setFriend(dave);
        repository.save(carter);
        assertThat(dave.getAge()).isEqualTo(42);

        List<Person> result = repository.findByFriendAge(42);

        assertThat(result)
            .hasSize(1)
            .containsExactly(carter);

        TestUtils.setFriendsToNull(repository, oliver, dave, carter);
    }

    @Test
    public void findPersonsByFriendAgeNotEqual() {
        oliver.setFriend(alicia);
        repository.save(oliver);
        dave.setFriend(oliver);
        repository.save(dave);
        carter.setFriend(dave);
        repository.save(carter);

        List<Person> result = repository.findByFriendAgeIsNot(42);

        assertThat(result)
            .hasSize(2)
            .containsExactlyInAnyOrder(dave, oliver);

        TestUtils.setFriendsToNull(repository, oliver, dave, carter);
    }

    @Test
    public void findPersonsByAddressZipCode() {
        String zipCode = "C0123456";
        carter.setAddress(new Address("Foo Street 2", 2, "C012344", "C0123"));
        repository.save(carter);
        dave.setAddress(new Address("Foo Street 1", 1, zipCode, "Bar"));
        repository.save(dave);
        boyd.setAddress(new Address(null, null, null, null));
        repository.save(boyd);

        List<Person> result = repository.findByAddressZipCode(zipCode);

        assertThat(result)
            .hasSize(1)
            .containsExactly(dave);
    }

    @Test
    public void findPersonsByFriendAgeGreaterThan() {
        alicia.setFriend(boyd);
        repository.save(alicia);
        dave.setFriend(oliver);
        repository.save(dave);
        carter.setFriend(dave);
        repository.save(carter);
        leroi.setFriend(carter);
        repository.save(leroi);

        assertThat(alicia.getFriend().getAge()).isGreaterThan(42);
        assertThat(leroi.getFriend().getAge()).isGreaterThan(42);

        List<Person> result = repository.findByFriendAgeGreaterThan(42);

        assertThat(result)
            .hasSize(2)
            .containsExactlyInAnyOrder(alicia, leroi);

        TestUtils.setFriendsToNull(repository, alicia, dave, carter, leroi);
    }

    @Test
    public void findPersonsByFriendAgeLessThanOrEqual() {
        alicia.setFriend(boyd);
        repository.save(alicia);
        dave.setFriend(oliver);
        repository.save(dave);
        carter.setFriend(dave);
        repository.save(carter);
        leroi.setFriend(carter);
        repository.save(leroi);

        List<Person> result = repository.findByFriendAgeLessThanEqual(42);

        assertThat(result)
            .hasSize(2)
            .containsExactlyInAnyOrder(dave, carter);

        TestUtils.setFriendsToNull(repository, alicia, dave, carter, leroi);
    }

    @Test
    public void findAll_doesNotFindDeletedPersonByEntity() {
        try {
            repository.delete(dave);
            List<Person> result = (List<Person>) repository.findAll();
            assertThat(result)
                .doesNotContain(dave)
                .containsExactlyInAnyOrderElementsOf(
                    allPersons.stream().filter(person -> !person.equals(dave)).collect(Collectors.toList())
                );
        } finally {
            repository.save(dave);
        }
    }

    @Test
    public void findAll_doesNotFindDeletedPersonById() {
        try {
            repository.deleteById(dave.getId());
            List<Person> result = (List<Person>) repository.findAll();
            assertThat(result)
                .doesNotContain(dave)
                .hasSize(allPersons.size() - 1);
        } finally {
            repository.save(dave);
        }
    }

    @Test
    public void findPersonsByFirstName() {
        List<Person> result = repository.findByFirstName("Leroi");
        assertThat(result).hasSize(2).containsOnly(leroi, leroi2);

        List<Person> result1 = repository.findByFirstNameIgnoreCase("lEroi");
        assertThat(result1).hasSize(2).containsOnly(leroi, leroi2);

        List<Person> result2 = repository.findByFirstName("lEroi");
        assertThat(result2).hasSize(0);
    }

    @Test
    public void findPersonsByFirstNameNot() {
        List<Person> result = repository.findByFirstNameNot("Leroi");
        assertThat(result).doesNotContain(leroi, leroi2);

        List<Person> result1 = repository.findByFirstNameNotIgnoreCase("lEroi");
        assertThat(result1).doesNotContain(leroi, leroi2);

        List<Person> result2 = repository.findByFirstNameNot("lEroi");
        assertThat(result2).contains(leroi, leroi2);
    }

    @Test
    public void findPersonsByFirstNameGreaterThan() {
        List<Person> result = repository.findByFirstNameGreaterThan("Leroa");
        assertThat(result).contains(leroi, leroi2);
    }

    @Test
    public void findByLastNameNot_forExistingResult() {
        Stream<Person> result = repository.findByLastNameNot("Moore");

        assertThat(result)
            .doesNotContain(leroi, leroi2)
            .contains(dave, donny, oliver, carter, boyd, stefan, alicia);
    }

    @Test
    public void findByFirstNameNotIn_forEmptyResult() {
        Set<String> allFirstNames = allPersons.stream().map(Person::getFirstName).collect(Collectors.toSet());
//		Stream<Person> result = repository.findByFirstnameNotIn(allFirstNames);
        assertThatThrownBy(() -> repository.findByFirstNameNotIn(allFirstNames))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported keyword 'NOT_IN (1): [IsNotIn, NotIn]'");

//		assertThat(result).isEmpty();
    }

    @Test
    public void findByFirstNameNotIn_forExistingResult() {
//		Stream<Person> result = repository.findByFirstnameNotIn(Collections.singleton("Alicia"));
        assertThatThrownBy(() -> repository.findByFirstNameNotIn(Collections.singleton("Alicia")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("Unsupported keyword 'NOT_IN (1): [IsNotIn, NotIn]'");

//		assertThat(result).contains(dave, donny, oliver, carter, boyd, stefan, leroi, leroi2);
    }

    @Test
    public void findByFirstNameIn_forEmptyResult() {
        Stream<Person> result = repository.findByFirstNameIn(List.of("Anastasiia", "Daniil"));

        assertThat(result).isEmpty();
    }

    @Test
    public void findByFirstNameIn_forExistingResult() {
        Stream<Person> result = repository.findByFirstNameIn(List.of("Alicia", "Stefan"));

        assertThat(result).contains(alicia, stefan);
    }

    @Test
    public void countByLastName_forExistingResult() {
        assertThatThrownBy(() -> repository.countByLastName("Leroi"))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Query method Person.countByLastName not supported.");

//		assertThat(result).isEqualTo(2);
    }

    @Test
    public void countByLastName_forEmptyResult() {
        assertThatThrownBy(() -> repository.countByLastName("Smirnova"))
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Query method Person.countByLastName not supported.");

//		assertThat(result).isEqualTo(0);
    }

    @Test
    public void findByAgeGreaterThan_forExistingResult() {
        Slice<Person> slice = repository.findByAgeGreaterThan(40, PageRequest.of(0, 10));

        assertThat(slice.hasContent()).isTrue();
        assertThat(slice.hasNext()).isFalse();
        assertThat(slice.getContent()).hasSize(4).contains(dave, carter, boyd, leroi);
    }

    @Test
    public void findPersonsSomeFieldsByAgeGreaterThan_forExistingResultProjection() {
        Slice<PersonSomeFields> slice = repository.findPersonSomeFieldsByAgeGreaterThan(
            40, PageRequest.of(0, 10)
        );

        assertThat(slice.hasContent()).isTrue();
        assertThat(slice.hasNext()).isFalse();
        assertThat(slice.getContent()).hasSize(4).contains(dave.toPersonSomeFields(),
            carter.toPersonSomeFields(), boyd.toPersonSomeFields(), leroi.toPersonSomeFields());
    }

    @Test
    public void findByAgeGreaterThan_respectsLimit() {
        Slice<Person> slice = repository.findByAgeGreaterThan(40, PageRequest.of(0, 1));

        assertThat(slice.hasContent()).isTrue();
        assertThat(slice.hasNext()).isFalse(); // TODO: not implemented yet. should be true instead
        assertThat(slice.getContent()).containsAnyOf(dave, carter, boyd, leroi).hasSize(1);
    }

    @Test
    public void findByAgeGreaterThan_respectsLimitAndOffsetAndSort() {
        List<Person> result = IntStream.range(0, 4)
            .mapToObj(index -> repository.findByAgeGreaterThan(40, PageRequest.of(
                index, 1, Sort.by("age")
            )))
            .flatMap(slice -> slice.getContent().stream())
            .collect(Collectors.toList());

        assertThat(result)
            .hasSize(4)
            .containsSequence(dave, leroi, boyd, carter);
    }

    @Test
    public void findByAgeGreaterThan_returnsValidValuesForNextAndPrev() {
        Slice<Person> first = repository.findByAgeGreaterThan(40, PageRequest.of(0, 1, Sort.by("age")));

        assertThat(first.hasContent()).isTrue();
        assertThat(first.getNumberOfElements()).isEqualTo(1);
        assertThat(first.hasNext()).isFalse(); // TODO: not implemented yet. should be true instead
        assertThat(first.isFirst()).isTrue();

        Slice<Person> last = repository.findByAgeGreaterThan(40, PageRequest.of(3, 1, Sort.by("age")));

        assertThat(last.hasContent()).isTrue();
        assertThat(last.getNumberOfElements()).isEqualTo(1);
        assertThat(last.hasNext()).isFalse();
        assertThat(last.isLast()).isTrue();
    }

    @Test
    public void findByAgeGreaterThan_forEmptyResult() {
        Slice<Person> slice = repository.findByAgeGreaterThan(100, PageRequest.of(0, 10));

        assertThat(slice.hasContent()).isFalse();
        assertThat(slice.hasNext()).isFalse();
        assertThat(slice.getContent()).isEmpty();
    }

    @Test
    public void findByLastNameStartsWithOrderByAgeAsc_respectsLimitAndOffset() {
        Page<Person> first = repository.findByLastNameStartsWithOrderByAgeAsc("Mo", PageRequest.of(0, 1));

        assertThat(first.getNumberOfElements()).isEqualTo(1);
        assertThat(first.getTotalPages()).isEqualTo(2);
        assertThat(first.get()).hasSize(1).containsOnly(leroi2);

        Page<Person> last = repository.findByLastNameStartsWithOrderByAgeAsc("Mo", first.nextPageable());

        assertThat(last.getTotalPages()).isEqualTo(2);
        assertThat(last.getNumberOfElements()).isEqualTo(1);
        assertThat(last.get()).hasSize(1).containsAnyOf(leroi);

        Page<Person> all = repository.findByLastNameStartsWithOrderByAgeAsc("Mo", PageRequest.of(0, 5));

        assertThat(all.getTotalPages()).isEqualTo(1);
        assertThat(all.getNumberOfElements()).isEqualTo(2);
        assertThat(all.get()).hasSize(2).containsOnly(leroi, leroi2);
    }

    @Test
    public void findPersonsByFirstNameAndByAge() {
        List<Person> result = repository.findByFirstNameAndAge("Leroi", 25);
        assertThat(result).containsOnly(leroi2);

        result = repository.findByFirstNameAndAge("Leroi", 44);
        assertThat(result).containsOnly(leroi);
    }

    @Test
    public void findPersonsByFirstNameStartsWith() {
        List<Person> result = repository.findByFirstNameStartsWith("D");

        assertThat(result).containsOnly(dave, donny, douglas);
    }

    @Test
    public void findPersonsByFriendFirstNameStartsWith() {
        stefan.setFriend(oliver);
        repository.save(stefan);
        carter.setFriend(dave);
        repository.save(carter);

        List<Person> result = repository.findByFriendFirstNameStartsWith("D");
        assertThat(result)
            .hasSize(1)
            .containsExactly(carter);

        TestUtils.setFriendsToNull(repository, stefan, carter);
    }

    @Test
    public void findPersonsByFriendLastNameLike() {
        oliver.setFriend(dave);
        repository.save(oliver);
        carter.setFriend(stefan);
        repository.save(carter);

        List<Person> result = repository.findByFriendLastNameLike(".*tthe.*");
        assertThat(result).contains(oliver);
        TestUtils.setFriendsToNull(repository, oliver, carter);
    }

    @Test
    public void findPagedPersons() {
        Page<Person> result = repository.findAll(PageRequest.of(
            1, 2, Sort.Direction.ASC, "lastname", "firstname")
        );
        assertThat(result.isFirst()).isFalse();
        assertThat(result.isLast()).isFalse();
    }

    @Test
    public void findPersonInAgeRangeCorrectly() {
        Iterable<Person> it = repository.findByAgeBetween(40, 45);

        assertThat(it).hasSize(3).contains(dave);
    }

    @Test
    public void findPersonInAgeRangeCorrectlyOrderByLastName() {
        Iterable<Person> it = repository.findByAgeBetweenOrderByLastName(30, 45);

        assertThat(it).hasSize(6);
    }

    @Test
    public void findPersonInAgeRangeAndNameCorrectly() {
        Iterable<Person> it = repository.findByAgeBetweenAndLastName(40, 45, "Matthews");
        assertThat(it).hasSize(1);

        Iterable<Person> result = repository.findByAgeBetweenAndLastName(20, 26, "Moore");
        assertThat(result).hasSize(1);
    }

    @Test
    public void findPersonsByFriendsInAgeRangeCorrectly() {
        oliver.setFriend(alicia);
        repository.save(oliver);
        dave.setFriend(oliver);
        repository.save(dave);
        carter.setFriend(dave);
        repository.save(carter);

        List<Person> result = repository.findByFriendAgeBetween(40, 45);

        assertThat(result)
            .hasSize(1)
            .containsExactly(carter);

        TestUtils.setFriendsToNull(repository, oliver, dave, carter);
    }

    @Test
    public void findPersonsByStringsList() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            List<String> listToCompareWith = List.of("str1", "str2");
            assertThat(dave.getStrings()).isEqualTo(listToCompareWith);

            List<Person> persons = repository.findByStringsEquals(listToCompareWith);
            assertThat(persons).contains(dave);

            // another way to call the method
            List<Person> persons2 = repository.findByStrings(listToCompareWith);
            assertThat(persons2).contains(dave);
        }
    }

    @Test
    public void findPersonsByStringsListNotEqual() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            List<String> listToCompareWith = List.of("str1", "str2");
            assertThat(dave.getStrings()).isEqualTo(listToCompareWith);
            assertThat(donny.getStrings()).isNotEmpty();
            assertThat(donny.getStrings()).isNotEqualTo(listToCompareWith);

            List<Person> persons = repository.findByStringsIsNot(listToCompareWith);
            assertThat(persons).contains(donny);
        }
    }

    @Test
    public void findPersonsByStringsListLessThan() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            List<String> listToCompareWith = List.of("str1", "str2", "str3");
            List<String> listWithFewerElements = List.of("str1", "str2");
            assertThat(donny.getStrings()).isEqualTo(listToCompareWith);
            assertThat(dave.getStrings()).isEqualTo(listWithFewerElements);

            List<Person> persons = repository.findByStringsLessThan(listToCompareWith);
            assertThat(persons).contains(dave);
        }
    }

    @Test
    public void findPersonsByStringsListGreaterThanOrEqual() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            Set<Integer> setToCompareWith = Set.of(0, 1, 2, 3, 4);
            dave.setIntSet(setToCompareWith);
            repository.save(dave);
            assertThat(dave.getIntSet()).isEqualTo(setToCompareWith);

            List<Person> persons = repository.findByIntSetGreaterThanEqual(setToCompareWith);
            assertThat(persons).contains(dave);
        }
    }

    @Test
    public void findPersonsByStringMap() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            Map<String, String> mapToCompareWith = Map.of("key1", "val1", "key2", "val2");
            assertThat(boyd.getStringMap()).isEqualTo(mapToCompareWith);

            List<Person> persons = repository.findByStringMapEquals(mapToCompareWith);
            assertThat(persons).contains(boyd);

            // another way to call the method
            List<Person> persons2 = repository.findByStringMap(mapToCompareWith);
            assertThat(persons2).contains(boyd);
        }
    }

    @Test
    public void findPersonsByAddress() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            Address address = new Address("Foo Street 1", 1, "C0123", "Bar");
            dave.setAddress(address);
            repository.save(dave);

            List<Person> persons = repository.findByAddress(address);
            assertThat(persons).containsOnly(dave);
        }
    }

    @Test
    public void findPersonsByAddressNotEqual() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            Address address = new Address("Foo Street 1", 1, "C0123", "Bar");
            assertThat(dave.getAddress()).isEqualTo(address);
            assertThat(carter.getAddress()).isNotNull();
            assertThat(carter.getAddress()).isNotEqualTo(address);
            assertThat(boyd.getAddress()).isNotNull();
            assertThat(boyd.getAddress()).isNotEqualTo(address);

            List<Person> persons = repository.findByAddressIsNot(address);
            assertThat(persons).contains(carter, boyd);
        }
    }

    @Test
    public void findPersonsByIntMapNotEqual() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            Map<String, Integer> mapToCompareWith = Map.of("key1", 0, "key2", 1);
            assertThat(carter.getIntMap()).isEqualTo(mapToCompareWith);

            carter.setIntMap(Map.of("key1", 1, "key2", 2));
            repository.save(carter);
            assertThat(carter.getIntMap()).isNotEqualTo(mapToCompareWith);

            assertThat(repository.findByIntMapIsNot(mapToCompareWith)).contains(carter);

            carter.setIntMap(mapToCompareWith);
            repository.save(carter);
        }
    }

    @Test
    public void findPersonsByAddressLessThan() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            Address address = new Address("Foo Street 2", 2, "C0124", "C0123");
            assertThat(dave.getAddress()).isNotEqualTo(address);
            assertThat(carter.getAddress()).isEqualTo(address);

            List<Person> persons = repository.findByAddressLessThan(address);
            assertThat(persons).containsExactlyInAnyOrder(dave, boyd);
        }
    }

    @Test
    public void findPersonsByStringMapGreaterThan() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            assertThat(boyd.getStringMap()).isNotEmpty();
            assertThat(donny.getStringMap()).isNotEmpty();

            Map<String, String> mapToCompare = Map.of("Key", "Val", "Key2", "Val2");
            List<Person> persons = repository.findByStringMapGreaterThan(mapToCompare);
            assertThat(persons).containsExactlyInAnyOrder(boyd);
        }
    }

    @Test
    public void findPersonsByFriend() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            alicia.setAddress(new Address("Foo Street 1", 1, "C0123", "Bar"));
            repository.save(alicia);
            oliver.setFriend(alicia);
            repository.save(oliver);

            List<Person> persons = repository.findByFriend(alicia);
            assertThat(persons).containsOnly(oliver);
        }
    }

    @Test
    public void findPersonsByFriendAddress() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            Address address = new Address("Foo Street 1", 1, "C0123", "Bar");
            dave.setAddress(address);
            repository.save(dave);

            carter.setFriend(dave);
            repository.save(carter);

            List<Person> result = repository.findByFriendAddress(address);

            assertThat(result)
                .hasSize(1)
                .containsExactly(carter);

            TestUtils.setFriendsToNull(repository, carter);
        }
    }

    @Test
    public void findPersonsByFriendAddressZipCode() {
        String zipCode = "C012345";
        Address address = new Address("Foo Street 1", 1, zipCode, "Bar");
        dave.setAddress(address);
        repository.save(dave);

        carter.setFriend(dave);
        repository.save(carter);

        List<Person> result = repository.findByFriendAddressZipCode(zipCode);

        assertThat(result)
            .hasSize(1)
            .containsExactly(carter);

        TestUtils.setFriendsToNull(repository, carter);
    }

    @Test
    public void findPersonsByFriendFriendAddressZipCode() {
        String zipCode = "C0123";
        Address address = new Address("Foo Street 1", 1, zipCode, "Bar");
        dave.setAddress(address);
        repository.save(dave);

        carter.setFriend(dave);
        repository.save(carter);
        oliver.setFriend(carter);
        repository.save(oliver);

        List<Person> result = repository.findByFriendFriendAddressZipCode(zipCode);

        assertThat(result)
            .hasSize(1)
            .containsExactly(oliver);

        TestUtils.setFriendsToNull(repository, carter, oliver);
    }

    @Test
    // find by deeply nested String POJO field
    public void findPersonsByFriendFriendFriendFriendFriendFriendFriendFriendBestFriendFriendAddressZipCode() {
        String zipCode = "C0123";
        Address address = new Address("Foo Street 1", 1, zipCode, "Bar");
        dave.setAddress(address);
        repository.save(dave);

        alicia.setFriend(dave);
        repository.save(alicia);
        oliver.setBestFriend(alicia);
        repository.save(oliver);
        carter.setFriend(oliver);
        repository.save(carter);
        donny.setFriend(carter);
        repository.save(donny);
        boyd.setFriend(donny);
        repository.save(boyd);
        stefan.setFriend(boyd);
        repository.save(stefan);
        leroi.setFriend(stefan);
        repository.save(leroi);
        leroi2.setFriend(leroi);
        repository.save(leroi2);
        matias.setFriend(leroi2);
        repository.save(matias);
        douglas.setFriend(matias);
        repository.save(douglas);

        List<Person> result =
            repository.findByFriendFriendFriendFriendFriendFriendFriendFriendBestFriendFriendAddressZipCode(zipCode);

        assertThat(result)
            .hasSize(1)
            .containsExactly(douglas);

        TestUtils.setFriendsToNull(repository, allPersons.toArray(Person[]::new));
    }

    @Test
    // find by deeply nested Integer POJO field
    public void findPersonsByFriendFriendFriendFriendFriendFriendFriendFriendBestFriendAddressApartmentNumber() {
        int apartment = 10;
        Address address = new Address("Foo Street 1", apartment, "C0123", "Bar");
        alicia.setAddress(address);
        repository.save(alicia);

        oliver.setBestFriend(alicia);
        repository.save(oliver);
        carter.setFriend(oliver);
        repository.save(carter);
        donny.setFriend(carter);
        repository.save(donny);
        boyd.setFriend(donny);
        repository.save(boyd);
        stefan.setFriend(boyd);
        repository.save(stefan);
        leroi.setFriend(stefan);
        repository.save(leroi);
        leroi2.setFriend(leroi);
        repository.save(leroi2);
        douglas.setFriend(leroi2);
        repository.save(douglas);
        matias.setFriend(douglas);
        repository.save(matias);

        List<Person> result =
            repository.findByFriendFriendFriendFriendFriendFriendFriendFriendBestFriendAddressApartment(apartment);

        assertThat(result)
            .hasSize(1)
            .containsExactly(matias);

        TestUtils.setFriendsToNull(repository, allPersons.toArray(Person[]::new));
    }

    @Test
    // find by deeply nested POJO
    public void findPersonsByFriendFriendFriendFriendFriendFriendFriendFriendBestFriendBestFriendAddress() {
        if (IndexUtils.isFindByPojoSupported(client)) {
            Address address = new Address("Foo Street 1", 1, "C0123", "Bar");
            dave.setAddress(address);
            repository.save(dave);

            alicia.setBestFriend(dave);
            repository.save(alicia);
            oliver.setBestFriend(alicia);
            repository.save(oliver);
            carter.setFriend(oliver);
            repository.save(carter);
            donny.setFriend(carter);
            repository.save(donny);
            boyd.setFriend(donny);
            repository.save(boyd);
            stefan.setFriend(boyd);
            repository.save(stefan);
            leroi.setFriend(stefan);
            repository.save(leroi);
            matias.setFriend(leroi);
            repository.save(matias);
            douglas.setFriend(matias);
            repository.save(douglas);
            leroi2.setFriend(douglas);
            repository.save(leroi2);

            List<Person> result =
                repository.findByFriendFriendFriendFriendFriendFriendFriendFriendBestFriendBestFriendAddress(address);

            assertThat(result)
                .hasSize(1)
                .containsExactly(leroi2);

            TestUtils.setFriendsToNull(repository, allPersons.toArray(Person[]::new));
        }
    }
}
