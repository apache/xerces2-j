/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
This SVN branch contains Xerces proprietary (other than W3C XML Schema 1.1 test suite) unit test cases for the 
Xerces's XML Schema 1.1 implementation.

Configuration notes: Minimum JRE needed to run these tests is 1.5. These unit tests contain a folder "data" parallel 
to the "src" folder, that contain the XML and XSD documents needed for the unit tests. Before invoking this unit test 
suite, the following argument needs to be provided to the Java VM,
-Dorg.apache.xerces.tests.dataDir=<path to data directory>
relative to which the unit tests would retrieve XML and XSD documents.

The class "AllXercesXSD11Tests" is the entry point of this unit test suite.

Acknowledgements:
These unit tests contain, XSD 1.1 examples written by Roger L. Costello (costello@mitre.org), and have been included
here with the permission from author. Roger's contributions of useful XSD 1.1 examples to this test suite is highly appreciated.
Roger has been an active contributor to the XML Schema standards work.