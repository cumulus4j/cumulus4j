====
    Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
    Copyright (C) 2011 NightLabs Consulting GmbH

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
====

Instructions for running tests
==============================
To run tests against RDBMS make sure the applicable "cumulus4j-test-datanucleus.properties" is defined for RDBMS. Then run "mvn clean test".

To run tests against MongoDB make sure the applicable "cumulus4j-test-datanucleus.properties" is defined for MongoDB. Then run "mvn clean test -Pmongodb".
