package io.chthonic.storeminal.domain.model

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test

class InputStringTests {

    @Test
    fun `given charsequence is empty when validate then InputString is null`() {
        // given
        val s: CharSequence = ""

        // when/then
        InputString.validateOrNull(s).shouldBeNull()
    }

    @Test
    fun `given charsequence is blank when validate then InputString is null`() {
        // given
        val s: CharSequence = "  "

        // when/then
        InputString.validateOrNull(s).shouldBeNull()
    }

    @Test
    fun `given charsequence is non-blank when validate then InputString is nonnull and has expected text`() {
        // given
        val s: CharSequence = "foo"

        // when
        val input = InputString.validateOrNull(s)

        // then
        input.shouldNotBeNull()
        input.text.shouldBeEqualTo("foo")
    }

    @Test
    fun `given charsequence is non-blank with surrounding white space when validate then InputString is nonnull and expected text without surrounding whitespace`() {
        // given
        val s: CharSequence = "  foo  "

        // when
        val input = InputString.validateOrNull(s)

        // then
        input.shouldNotBeNull()
        input.text.shouldBeEqualTo("foo")
    }
}