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

import org.springframework.data.mapping.PersistentProperty;

public interface AerospikePersistentProperty extends PersistentProperty<AerospikePersistentProperty> {

    /**
     * @return whether property access shall be used for reading the property value. This means it will use the getter
     * instead of field access.
     */
    boolean usePropertyAccess();

    /**
     * @return whether id property is explicit
     */
    boolean isExplicitIdProperty();

    /**
     * @return whether expiration property is present
     */
    boolean isExpirationProperty();

    /**
     * @return whether expiration is set as Unix timestamp
     */
    boolean isExpirationSpecifiedAsUnixTime();

    /**
     * @return the field name to be used to store the value of the property.
     */
    String getFieldName();
}
