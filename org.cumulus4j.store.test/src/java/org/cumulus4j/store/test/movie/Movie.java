/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.cumulus4j.store.test.movie;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class Movie
{
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long movieID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String name;

	@Join
	private Set<Person> starring = new HashSet<Person>();

	@Join
	private Set<Person> writtenBy = new HashSet<Person>();

	@Join
	private Set<Language> languages = new HashSet<Language>();

	@Join
	private Set<Person> directedBy = new HashSet<Person>();

	@Join
	private Set<Person> producedBy = new HashSet<Person>();

	@Column(jdbcType="CLOB")
	private String tagLine;

	private Rating rating;

	public long getMovieID() {
		return movieID;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Set<Person> getStarring() {
		return starring;
	}

	public Set<Person> getWrittenBy() {
		return writtenBy;
	}

	public Set<Language> getLanguages() {
		return languages;
	}

	public Set<Person> getDirectedBy() {
		return directedBy;
	}

	public Set<Person> getProducedBy() {
		return producedBy;
	}

	public String getTagLine() {
		return tagLine;
	}

	public void setTagLine(String tagLine) {
		this.tagLine = tagLine;
	}

	public Rating getRating() {
		return rating;
	}

	public void setRating(Rating rating) {
		this.rating = rating;
	}

	@Override
	public int hashCode() {
		return (int) (movieID ^ (movieID >>> 32));
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (this.movieID < 0) return false;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;

		Movie other = (Movie) obj;
		if (movieID != other.movieID)
			return false;
		return true;
	}
}
