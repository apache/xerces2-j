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

package org.apache.xerces.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @xerces.internal
 * 
 * @author: Mukul Gandhi IBM
 * @version $Id$
 */
public class AllXercesXSD11Tests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.apache.xerces.tests");
		//$JUnit-BEGIN$
		suite.addTestSuite(GenericTests.class);
		suite.addTestSuite(AssertionTests.class);
		suite.addTestSuite(TypeAlternativeTests.class);
		suite.addTestSuite(OpenContentTests.class);
		suite.addTestSuite(TargetNamespaceTests.class);
		suite.addTestSuite(ConditionalInclusionTests.class);
		suite.addTestSuite(SubstitutionGroupTests.class);
		suite.addTestSuite(AttributeTests.class);
		suite.addTestSuite(CompositorTests.class);
		suite.addTestSuite(SimpleTypeTests.class);
		suite.addTestSuite(RedefineTests.class);
		suite.addTestSuite(OverrideTests.class);
		suite.addTestSuite(PSVITests.class);
		suite.addTestSuite(IDConstraintTests.class);
		suite.addTestSuite(JiraBugsTests.class);
		//$JUnit-END$
		return suite;
	}

}
