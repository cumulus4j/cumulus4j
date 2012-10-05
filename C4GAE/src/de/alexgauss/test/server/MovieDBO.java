package de.alexgauss.test.server;

import java.util.List;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.annotation.NotQueryable;

@PersistenceCapable
public class MovieDBO {

	@PrimaryKey
	@NotQueryable
    @Persistent(valueStrategy = IdGeneratorStrategy.SEQUENCE)
    private long key;

    @Persistent
    private String title;

    @Persistent
    private String subtitle;
    
    @Persistent
    private List<String> starring;
	
    public MovieDBO(String movieTitle, String movieSubtitle, List<String> movieStarring){
    	title = movieTitle;
    	subtitle = movieSubtitle;
    	starring = movieStarring;
    }
    
    public long getKey() {
        return key;
    }

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public void setSubtitle(String subtitle) {
		this.subtitle = subtitle;
	}

	public List<String> getStarring() {
		return starring;
	}

	public void setStarring(List<String> starring) {
		this.starring = starring;
	}
    
}
