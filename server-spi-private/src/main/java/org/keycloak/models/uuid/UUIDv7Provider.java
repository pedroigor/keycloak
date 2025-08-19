/*
 * Copyright 2025 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.keycloak.models.uuid;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.UUID;

public class UUIDv7Provider implements UUIDProvider {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    public String generateUUID() {
        return generateIdv7();
    }

    /**
     * Generate a UUID v7 (time-ordered UUID)
     *
     * @return UUID v7 instance
     */
    public static String generateIdv7() {
        long timestamp = Instant.now().toEpochMilli();

        // Generate random bytes for the remaining parts
        byte[] randomBytes = new byte[10];
        SECURE_RANDOM.nextBytes(randomBytes);

        // Construct MSB: 48-bit timestamp + 4-bit version + 12-bit random
        long msb = (timestamp << 16) |
                (0x7L << 12) |
                (((long) randomBytes[0] & 0x0F) << 8) |
                ((long) randomBytes[1] & 0xFF);

        // Construct LSB: 2-bit variant + 62-bit random
        long lsb = (0x8L << 60) |
                ((long) (randomBytes[2] & 0x3F) << 56) |
                ((long) (randomBytes[3] & 0xFF) << 48) |
                ((long) (randomBytes[4] & 0xFF) << 40) |
                ((long) (randomBytes[5] & 0xFF) << 32) |
                ((long) (randomBytes[6] & 0xFF) << 24) |
                ((long) (randomBytes[7] & 0xFF) << 16) |
                ((long) (randomBytes[8] & 0xFF) << 8) |
                (randomBytes[9] & 0xFF);

        UUID uuid = new UUID(msb, lsb);
        return uuid.toString();
    }

    @Override
    public void close() {
    }
}
