package codes.quine.labo.re2s

import minitest.SimpleTestSuite

object HelloSuite extends SimpleTestSuite {
  test("Hello.world") {
    assertEquals(Hello.world, "Hello World")
  }
}
