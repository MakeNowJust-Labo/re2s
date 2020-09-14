package codes.quine.labo

package object re2s {
  @inline implicit def augmentStringRE2S(s: String): StringOps = new StringOps(s)
}
