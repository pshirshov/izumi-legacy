package org.bitbucket.pshirshov.izumitk.json.model

import com.fasterxml.jackson.annotation.JsonValue
import org.bitbucket.pshirshov.izumitk.model.Identifier

trait JIdentifier extends Identifier {
  @JsonValue
  override def asString: String
}


