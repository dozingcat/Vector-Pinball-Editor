# Vector Pinball Editor

This is a GUI editor which can create and edit pinball tables for the [Vector Pinball Android app](https://github.com/dozingcat/Vector-Pinball). It runs anywhere that supports Java 8. It is released under version 3 of the GPL; see "LICENSE.txt" for the license text.

Using this editor you can create new tables from scratch, or use the existing Vector Pinball tables as a starting point. You can test tables as you're designing them, using the same physics engine as the Android app (Box2D, provided by libgdx). You can also customize the table logic with Groovy scripts.

It is in an approximately alpha state; it basically works but there are almost certainly bugs. The UI is built with JavaFX, which needs to be separately installed on later JDK versions.

Currently exporting tables to the Vector Pinball app is a manual process. Vector Pinball currently only supports built-in tables, so you would need to download its source and copy your table to its assets/tables directory. I would like to add support for dynamically installing new tables in the future. In the meantime, feel free to send me (bnenning@gmail.com) any fun tables that you create, for possible inclusion in new Vector Pinball releases.

Vector Pinball includes the following components used under the terms of [Version 2.0 of the Apache License](http://www.apache.org/licenses/LICENSE-2.0):
*  [libgdx](http://libgdx.badlogicgames.com/) by Bad Logic Games
*  [Groovy](http://groovy-lang.org/) by the Groovy Project
*  [JSON.simple](https://code.google.com/p/json-simple/)