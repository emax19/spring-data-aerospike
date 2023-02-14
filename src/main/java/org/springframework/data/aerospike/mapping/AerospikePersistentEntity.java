/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.aerospike.mapping;

import org.springframework.data.mapping.PersistentEntity;

/**
 * Aerospike-specific extensions of {@link PersistentEntity}.
 *
 * @author Oliver Gierke
 * @author Peter Milne
 */
public interface AerospikePersistentEntity<T> extends PersistentEntity<T, AerospikePersistentProperty> {

    /**
     * @return set name
     */
    String getSetName();


    /**
     * @return expiration value in seconds
     */
    int getExpiration();

    /**
     * @return value of an optional {@link Document#touchOnRead()} flag
     */
    boolean isTouchOnRead();

    /**
     * @return expiration property
     */
    AerospikePersistentProperty getExpirationProperty();

    /**
     * @return true if there is expiration property
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean hasExpirationProperty();
}
