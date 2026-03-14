/*
 * Copyright 2015 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dv8tion.jda.internal.utils.config;

import net.dv8tion.jda.internal.utils.Checks;

import javax.annotation.Nonnull;

public final class AuthorizationConfig
{
    private char[] token;

    public AuthorizationConfig(@Nonnull String token)
    {
        Checks.notEmpty(token, "Token");
        Checks.noWhitespace(token, "Token");
        setToken(token.toCharArray());
    }

    // New constructor to accept char[] directly
    public AuthorizationConfig(@Nonnull char[] token)
    {
        notEmpty(token, "Token");
        // Temporarily convert to String for whitespace check, as Checks.noWhitespace expects String
        Checks.noWhitespace(new String(token), "Token");
        setToken(token);
    }

    @Nonnull
    public String getToken()
    {
        // Return a copy as a String. The caller should be aware that this creates a String
        // and should ideally clear it if possible after use.
        // For internal use where a char[] is needed, a new method could be added.
        return new String(token);
    }

    public void setToken(@Nonnull char[] token)
    {
        // Prepend "Bot " to the token.
        // This requires creating a new char array.
        char[] botPrefix = "Bot ".toCharArray();
        this.token = new char[botPrefix.length + token.length];
        System.arraycopy(botPrefix, 0, this.token, 0, botPrefix.length);
        System.arraycopy(token, 0, this.token, botPrefix.length, token.length);
        // The original token array passed to this method is assumed to be a copy or
        // will be cleared by the caller (e.g., JDABuilder).
    }

    // Helper for Checks.notEmpty with char[] (if Checks class doesn't have one)
    private static void notEmpty(@Nonnull char[] value, @Nonnull String name)
    {
        if (value == null || value.length == 0)
            throw new IllegalArgumentException(name + " may not be empty");
    }
}
