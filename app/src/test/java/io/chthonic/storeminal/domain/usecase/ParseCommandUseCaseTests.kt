package io.chthonic.storeminal.domain.usecase

import io.chthonic.storeminal.domain.error.UnknownCommandException
import io.chthonic.storeminal.domain.model.Command.*
import io.chthonic.storeminal.domain.model.Command.Set
import io.chthonic.storeminal.domain.model.InputString
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Test

internal class ParseCommandUseCaseTests {
    val tested = ParseCommandUseCase()

    @Test(expected = UnknownCommandException::class)
    fun `given input with more than 3 tokens when execute then throw UnknownCommandException`() {
        // given
        val input = InputString.validateOrNull("set fus do rah")!!

        // when/then
        tested.execute(input)
    }

    @Test(expected = UnknownCommandException::class)
    fun `given input with 1 token with unknown command when execute then throw UnknownCommandException`() {
        // given
        val input = InputString.validateOrNull("fus")!!

        // when/then
        tested.execute(input)
    }

    @Test(expected = UnknownCommandException::class)
    fun `given input with 2 tokens with unknown command when execute then throw UnknownCommandException`() {
        // given
        val input = InputString.validateOrNull("fus do")!!

        // when/then
        tested.execute(input)
    }

    @Test
    fun `given input with valid get tokens when execute then return Get command`() {
        // given
        val input = InputString.validateOrNull("get foo")!!

        // when/then
        tested.execute(input).shouldBeEqualTo(Get("foo"))
    }

    @Test
    fun `given input with valid set tokens when execute then return Set command`() {
        // given
        val input = InputString.validateOrNull("set foo bar")!!

        // when/then
        tested.execute(input).shouldBeEqualTo(Set("foo", "bar"))
    }

    @Test
    fun `given input with valid delete tokens when execute then return Delete command`() {
        // given
        val input = InputString.validateOrNull("delete foo")!!

        // when/then
        tested.execute(input).shouldBeEqualTo(Delete("foo"))
    }

    @Test
    fun `given input with valid count tokens when execute then return Count command`() {
        // given
        val input = InputString.validateOrNull("count foo")!!

        // when/then
        tested.execute(input).shouldBeEqualTo(Count("foo"))
    }

    @Test
    fun `given input with valid begin token when execute then return Begin command`() {
        // given
        val input = InputString.validateOrNull("begin")!!

        // when/then
        tested.execute(input).shouldBe(Begin)
    }

    @Test
    fun `given input with valid commit token when execute then return Commit command`() {
        // given
        val input = InputString.validateOrNull("commit")!!

        // when/then
        tested.execute(input).shouldBe(Commit)
    }

    @Test
    fun `given input with valid commit token when execute then return Rollback command`() {
        // given
        val input = InputString.validateOrNull("rollback")!!

        // when/then
        tested.execute(input).shouldBe(Rollback)
    }

    @Test
    fun `given valid command input with multiple spaces between tokens when execute then return expected command`() {
        // given
        val input = InputString.validateOrNull("get         foo")!!

        // when/then
        tested.execute(input).shouldBeEqualTo(Get("foo"))
    }

    @Test
    fun `given valid command input command token in caps when execute then return expected command`() {
        // given
        val input = InputString.validateOrNull("GET foo")!!

        // when/then
        tested.execute(input).shouldBeEqualTo(Get("foo"))
    }
}