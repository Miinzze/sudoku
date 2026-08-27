package com.sudokuai.app.data

import com.sudokuai.app.data.update.UpdateChecker
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateCheckerTest {

    @Test
    fun `higher major version is newer`() {
        assertTrue(UpdateChecker.isNewerVersion("2.0.0", "1.9.9"))
    }

    @Test
    fun `numeric comparison beats lexicographic comparison`() {
        // A plain string compare would (wrongly) say "1.10.0" < "1.9.0".
        assertTrue(UpdateChecker.isNewerVersion("1.10.0", "1.9.0"))
        assertFalse(UpdateChecker.isNewerVersion("1.9.0", "1.10.0"))
    }

    @Test
    fun `equal versions are not newer`() {
        assertFalse(UpdateChecker.isNewerVersion("1.0.0", "1.0.0"))
    }

    @Test
    fun `lower version is not newer`() {
        assertFalse(UpdateChecker.isNewerVersion("1.0.0", "1.0.1"))
    }
}
