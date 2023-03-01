package org.model;

import java.util.List;

public class Tag {

    private String tag;
    private List<String> followers;

    public Tag() {
    }

    public Tag(String tag, List<String> followers) {
        this.tag = tag;
        this.followers = followers;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }
}
