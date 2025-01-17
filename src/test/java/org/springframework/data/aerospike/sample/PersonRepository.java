/*
 * Copyright 2012-2021 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 		https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.aerospike.sample;

import org.springframework.data.aerospike.repository.AerospikeRepository;
import org.springframework.data.aerospike.repository.query.CriteriaDefinition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author Peter Milne
 * @author Jean Mercier
 */
public interface PersonRepository<P extends Person> extends AerospikeRepository<P, String> {

    List<P> findByLastName(String lastName);

    // DTO Projection
    List<PersonSomeFields> findPersonSomeFieldsByLastName(String lastName);

    // Dynamic Projection
    <T> List<T> findByLastName(String lastName, Class<T> type);

    Page<P> findByLastNameStartsWithOrderByAgeAsc(String prefix, Pageable pageable);

    List<P> findByLastNameEndsWith(String postfix);

    List<P> findByLastNameOrderByFirstNameAsc(String lastName);

    List<P> findByLastNameOrderByFirstNameDesc(String lastName);

    /**
     * Find all entities with firstName matching the given regex. POSIX Extended Regular Expression syntax is used to
     * interpret the regex.
     *
     * @param firstNameRegex Regex to find matching firstName
     */
    List<P> findByFirstNameLike(String firstNameRegex);

    List<P> findByFirstNameLikeIgnoreCase(String firstNameRegex);

    List<P> findByFirstNameLikeOrderByLastNameAsc(String firstName, Sort sort);

    /**
     * Find all entities with age less than the given numeric parameter
     *
     * @param age  integer to compare with
     * @param sort sorting
     */
    List<P> findByAgeLessThan(int age, Sort sort);

    /**
     * Find all entities with age less than the given numeric parameter
     *
     * @param age  long to compare with, [Long.MIN_VALUE+1..Long.MAX_VALUE]
     * @param sort sorting
     */
    List<P> findByAgeLessThan(long age, Sort sort);

    Stream<P> findByFirstNameIn(List<String> firstNames);

    @SuppressWarnings("UnusedReturnValue")
    Stream<P> findByFirstNameNotIn(Collection<String> firstNames);

    List<P> findByFirstNameAndLastName(String firstName, String lastName);

    List<P> findByAgeBetween(int from, int to);

    /**
     * Find all entities that satisfy the condition "have a friend equal to the given argument" (find by POJO)
     *
     * @param friend - Friend to check for equality
     */
    List<P> findByFriend(Person friend);

    /**
     * Find all entities that satisfy the condition "have address equal to the given argument" (find by POJO)
     *
     * @param address - Address to check for equality
     */
    List<P> findByAddress(Address address);

    /**
     * Find all entities that satisfy the condition "have existing address not equal to the given argument" (find by
     * POJO)
     *
     * @param address - Address to compare with
     */
    List<P> findByAddressIsNot(Address address);

    /**
     * Find all entities that satisfy the condition "have Address with fewer elements or with a corresponding key-value
     * lower in ordering than in the given argument" (find by POJO).
     * <p>
     * <a href="https://docs.aerospike.com/server/guide/data-types/cdt-ordering">Information about ordering</a>
     *
     * @param address - Address to compare with
     */
    List<P> findByAddressLessThan(Address address);

    List<P> findByAddressZipCode(String zipCode);

    List<P> findByAddressZipCodeContaining(String str);

    List<P> findByFirstNameContaining(String str);

    List<P> findByLastNameLikeAndAgeBetween(String lastName, int from, int to);

    List<P> findByAgeOrLastNameLikeAndFirstNameLike(int age, String lastName, String firstName);

//	List<P> findByNamedQuery(String firstName);

    List<P> findByCreator(User user);

    List<P> findByCreatedAtLessThan(Date date);

    List<P> findByCreatedAtGreaterThan(Date date);

//	List<P> findByCreatedAtLessThanManually(Date date);

    List<P> findByDateOfBirthBefore(Date date);

    List<P> findByDateOfBirthAfter(Date date);

    List<P> findByRegDate(LocalDate date);

    List<P> findByRegDateBefore(LocalDate date);

    List<P> findByCreatedAtAfter(Date date);

    Stream<P> findByLastNameNot(String lastName);

    List<P> findByCredentials(Credentials credentials);

    List<P> findCustomerByAgeBetween(int from, int to);

    List<P> findByAgeIn(ArrayList<Integer> ages);

    List<P> findPersonByFirstName(String firstName);

    List<P> findPersonsByActive(boolean isActive);

    List<P> findPersonsByActiveAndFirstName(boolean isActive, String firstName);

    @SuppressWarnings("UnusedReturnValue")
    long countByLastName(String lastName);

    int countByFirstName(String firstName);

    long someCountQuery(String lastName);

    List<P> findByFirstNameIgnoreCase(String firstName);

    List<P> findByFirstNameNotIgnoreCase(String firstName);

    List<P> findByFirstNameStartingWithIgnoreCase(String firstName);

    List<P> findByFirstNameEndingWithIgnoreCase(String firstName);

    List<P> findByFirstNameContainingIgnoreCase(String firstName);

    /**
     * Find all entities with age greater than the given numeric parameter
     *
     * @param age      integer to compare with
     * @param pageable Pageable
     */
    Slice<P> findByAgeGreaterThan(int age, Pageable pageable);

    /**
     * Find all entities with age greater than the given numeric parameter
     *
     * @param age      long to compare with, [Long.MIN_VALUE..Long.MAX_VALUE-1]
     * @param pageable Pageable
     */
    Slice<P> findByAgeGreaterThan(long age, Pageable pageable);

    // DTO Projection
    Slice<PersonSomeFields> findPersonSomeFieldsByAgeGreaterThan(int age, Pageable pageable);

    List<P> deleteByLastName(String lastName);

    Long deletePersonByLastName(String lastName);

    Page<P> findByAddressIn(List<Address> address, Pageable page);

    /**
     * Find all entities that satisfy the condition "have strings the same as the given argument" (find by list)
     *
     * @param list List to compare strings with
     */
    List<P> findByStringsEquals(List<String> list);

    /**
     * Find all entities that satisfy the condition "have strings the same as the given argument" (find by collection)
     *
     * @param collection Collection to compare strings with
     */
    List<P> findByStrings(Collection<String> collection);

    /**
     * Find all entities with existing strings list not equal to the given argument
     *
     * @param list List to compare strings list with
     */
    List<P> findByStringsIsNot(List<String> list);

    /**
     * Find all entities that satisfy the condition "have strings list with fewer elements or with a corresponding
     * element lower in ordering than in the given argument" (find by list).
     * <p>
     * <a href="https://docs.aerospike.com/server/guide/data-types/cdt-ordering">Information about ordering</a>
     *
     * @param list - List to compare with
     */
    List<P> findByStringsLessThan(List<String> list);

    /**
     * Find all entities that satisfy the condition "have strings set with fewer elements or with a corresponding
     * element lower in ordering than in the given argument" (find by collection).
     * <p>
     * <a href="https://docs.aerospike.com/server/guide/data-types/cdt-ordering">Information about ordering</a>
     *
     * @param collection - Collection to compare with
     */
    List<P> findByIntSetGreaterThanEqual(Collection<Integer> collection);

    /**
     * Find all entities containing the given map element (key or value depending on the given criteria)
     *
     * @param element  map element
     * @param criteria KEY or VALUE
     */
    List<P> findByStringMapContaining(String element, CriteriaDefinition.AerospikeMapCriteria criteria);

    /**
     * Find all entities that satisfy the condition "have exactly the given map key and the given value"
     *
     * @param key   Map key
     * @param value Value of the key
     */
    List<P> findByStringMapEquals(String key, String value);

    /**
     * Find all entities that satisfy the condition "have exactly the given map key and the given value"
     *
     * @param key   Map key
     * @param value Value of the key
     */
    List<P> findByStringMap(String key, String value);

    /**
     * Find all entities that satisfy the condition "have stringMap the same as the given argument" (find by map)
     *
     * @param map Map to compare stringMap with
     */
    List<P> findByStringMapEquals(Map<String, String> map);

    /**
     * Find all entities that satisfy the condition "have stringMap the same as the given argument" (find by map)
     *
     * @param map Map to compare stringMap with
     */
    List<P> findByStringMap(Map<String, String> map);

    /**
     * Find all entities that satisfy the condition "have stringMap with more elements or with a corresponding key-value
     * higher in ordering than in the given argument" (find by map).
     * <p>
     * <a href="https://docs.aerospike.com/server/guide/data-types/cdt-ordering">Information about ordering</a>
     *
     * @param map - Map to compare with
     */
    List<P> findByStringMapGreaterThan(Map<String, String> map);

    /**
     * Find all entities that satisfy the condition "have exactly the given map key and the given value"
     *
     * @param key   Map key
     * @param value Value of the key
     */
    List<P> findByIntMapEquals(String key, int value);

    /**
     * Find all entities with existing intMap not equal to the given argument
     *
     * @param map Map to compare intMap with
     */
    List<P> findByIntMapIsNot(Map<String, Integer> map);

    /**
     * Find all entities that satisfy the condition "have the given map key and NOT the given value"
     *
     * @param key   Map key
     * @param value Value of the key
     */
    List<P> findByIntMapIsNot(String key, int value);

    /**
     * Find all entities that satisfy the condition "have the given map key and the value that starts with the given
     * string"
     *
     * @param key             Map key
     * @param valueStartsWith String to check if map value starts with it
     */
    List<P> findByStringMapStartsWith(String key, String valueStartsWith);

    /**
     * Find all entities that satisfy the condition "have the given map key and the value matching the given regex"
     * POSIX Extended Regular Expression syntax is used to interpret the regex.
     *
     * @param key        Map key
     * @param valueRegex Regex to find matching map value
     */
    List<P> findByStringMapLike(String key, String valueRegex);

    /**
     * Find all entities that satisfy the condition "have the given map key and the value containing the given string"
     *
     * @param key       Map key
     * @param valuePart String to check if map value contains it
     */
    List<P> findByStringMapContaining(String key, String valuePart);

    /**
     * Find all entities that satisfy the condition "have the given map key and the value that is greater than the given
     * integer"
     *
     * @param key         Map key
     * @param greaterThan integer to check if map value is greater than it
     */
    List<P> findByIntMapGreaterThan(String key, int greaterThan);

    /**
     * Find all entities that satisfy the condition "have the given map key and the value that is less than or equal to
     * the given integer"
     *
     * @param key               Map key
     * @param lessThanOrEqualTo integer to check if map value satisfies the condition
     */
    List<P> findByIntMapLessThanEqual(String key, int lessThanOrEqualTo);

    /**
     * Find all entities that satisfy the condition "have the given map key and the value in between the given
     * integers"
     *
     * @param key  Map key
     * @param from the lower limit for the map value, inclusive
     * @param to   the upper limit for the map value, inclusive
     */
    List<P> findByIntMapBetween(String key, int from, int to);

    /**
     * Find all entities that satisfy the condition "have a bestFriend who has a friend with the given map key and the
     * value in between the given integers (deeply nested)"
     *
     * @param key  Map key
     * @param from the lower limit for the map value, inclusive
     * @param to   the upper limit for the map value, inclusive
     */
    List<P> findByBestFriendFriendIntMapBetween(String key, int from, int to);

    /**
     * Find all entities that satisfy the condition "have a bestFriend who has a friend with address apartment value
     * between the given integers (deeply nested)"
     *
     * @param from the lower limit for the map value, inclusive
     * @param to   the upper limit for the map value, inclusive
     */
    List<P> findByBestFriendFriendAddressApartmentBetween(int from, int to);

    List<P> findByFriendLastName(String value);

    /**
     * Find all entities that satisfy the condition "have a friend with the age equal to the given integer" (find by
     * POJO field)
     *
     * @param value - number to check for equality
     */
    List<P> findByFriendAge(int value);

    /**
     * Find all entities that satisfy the condition "have a friend with the existing age NOT equal to the given integer"
     * (find by POJO field)
     *
     * @param value - number to check for inequality
     */
    List<P> findByFriendAgeIsNot(int value);

    /**
     * Find all entities that satisfy the condition "have a friend with the age greater than the given integer" (find by
     * POJO field)
     *
     * @param value - lower limit, exclusive
     */
    List<P> findByFriendAgeGreaterThan(int value);

    /**
     * Find all entities that satisfy the condition "have a friend with the age less than or equal to the given integer"
     * (find by POJO field)
     *
     * @param value - upper limit, inclusive
     */
    List<P> findByFriendAgeLessThanEqual(int value);

    /**
     * Find all entities that satisfy the condition "have a friend with the age in the given range" (find by POJO
     * field)
     *
     * @param from lower limit, inclusive
     * @param to   upper limit, inclusive
     */
    List<P> findByFriendAgeBetween(int from, int to);

    /**
     * Find all entities that satisfy the condition "have a friend with the address equal to the given argument" (find
     * by inner POJO)
     *
     * @param address - Address to check for equality
     */
    List<P> findByFriendAddress(Address address);

    /**
     * Find all entities that satisfy the condition "have a friend with the address with zipCode equal to the given
     * argument" (find by nested POJO field)
     *
     * @param zipCode - Zip code to check for equality
     */
    List<P> findByFriendAddressZipCode(String zipCode);

    /**
     * Find all entities that satisfy the condition "have a friend who has bestFriend with the address with zipCode
     * equal to the given argument" (find by nested POJO field)
     *
     * @param zipCode - Zip code to check for equality
     */
    List<P> findByFriendBestFriendAddressZipCode(String zipCode);

    /**
     * Find all entities that satisfy the condition "have a friend who has bestFriend with the address with apartment
     * equal to the given argument" (find by nested POJO field)
     *
     * @param apartment - Apartment number to check for equality
     */
    List<P> findByFriendBestFriendAddressApartment(Integer apartment);

    /**
     * Find all entities that satisfy the condition "have a friend who has a friend with the address with zipCode equal
     * to the given argument" (find by POJO field)
     *
     * @param zipCode - Zip code to check for equality
     */
    List<P> findByFriendFriendAddressZipCode(String zipCode);

    /**
     * Find all entities that satisfy the condition "have a friend who has a friend (etc.) ... who has the address with
     * zipCode equal to the given argument" (find by deeply nested POJO field)
     *
     * @param zipCode - Zip code to check for equality
     */
    List<P> findByFriendFriendFriendFriendFriendFriendFriendFriendBestFriendFriendAddressZipCode(String zipCode);

    /**
     * Find all entities that satisfy the condition "have a friend who has a friend (etc.) ... who has the address with
     * apartment number equal to the given argument" (find by deeply nested POJO field)
     *
     * @param apartment - Integer to check for equality
     */
    List<P> findByFriendFriendFriendFriendFriendFriendFriendFriendBestFriendAddressApartment(Integer apartment);

    /**
     * Find all entities that satisfy the condition "have a friend who has a friend (etc.) ... who has the address equal
     * to the given argument" (find by deeply nested POJO)
     *
     * @param address - Address to check for equality
     */
    List<P> findByFriendFriendFriendFriendFriendFriendFriendFriendBestFriendBestFriendAddress(Address address);

    /**
     * Find all entities that satisfy the condition "have the list which contains the given string"
     * <p>
     * List name in this case is Strings
     * </p>
     *
     * @param string string to check
     */
    List<P> findByStringsContaining(String string);

    /**
     * Find all entities that satisfy the condition "have the list which contains the given integer"
     * <p>
     * List name in this case is Ints
     * </p>
     *
     * @param integer number to check
     */
    List<P> findByIntsContaining(int integer);

    /**
     * Find all entities that satisfy the condition "have at least one list value which is greater than the given
     * integer"
     * <p>
     * List name in this case is Ints
     * </p>
     *
     * @param integer upper limit, exclusive
     */
    List<P> findByIntsGreaterThan(int integer);

    /**
     * Find all entities that satisfy the condition "have at least one list value which is less than or equal to the
     * given integer"
     * <p>
     * List name in this case is Ints
     * </p>
     *
     * @param integer upper limit, inclusive
     */
    List<P> findByIntsLessThanEqual(int integer);

    /**
     * Find all entities that satisfy the condition "have at least one list value in the given range"
     * <p>
     * List name in this case is Ints
     * </p>
     *
     * @param from lower limit, inclusive
     * @param to   upper limit, inclusive
     */
    List<P> findByIntsBetween(int from, int to);

    List<P> findTop3ByLastNameStartingWith(String lastName);

    Page<P> findTop3ByLastNameStartingWith(String lastName, Pageable pageRequest);

    List<P> findByFirstName(String string);

    List<P> findByFirstNameNot(String string);

    /**
     * Find all entities that satisfy the condition "have firstName higher in ordering than the given string".
     * <p>
     * <a href="https://docs.aerospike.com/server/guide/data-types/cdt-ordering">Information about ordering</a>
     *
     * @param string - String to compare with
     */
    List<P> findByFirstNameGreaterThan(String string);

    List<P> findByFirstNameAndAge(String string, int i);

    Iterable<P> findByAgeBetweenAndLastName(int from, int to, String lastName);

    Iterable<P> findByAgeBetweenOrLastName(int from, int to, String lastName);

    List<P> findByFirstNameStartsWith(String string);

    List<P> findByFriendFirstNameStartsWith(String string);

    /**
     * Find all entities that satisfy the condition "have a friend with lastName matching the giving regex". POSIX
     * Extended Regular Expression syntax is used to interpret the regex.
     *
     * @param lastNameRegex Regex to find matching lastName
     */
    List<P> findByFriendLastNameLike(String lastNameRegex);

    Iterable<P> findByAgeBetweenOrderByLastName(int i, int j);
}
