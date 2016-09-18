package com.clarifai.androidstarter;

/**
 * Created by Bilal-PC on 010.10 Sep,15.
 */
public class Shop {

    private int id;
    private String name;
    private String address;
    private String tag;

    public Shop()
    {
    }

    public Shop(int id,String name,String address, String tag)
    {
        this.id=id;
        this.name=name;
        this.address=address;
        this.tag=tag;
    }

    public Shop(String name,String address, String tag)
    {
        this.name=name;
        this.address=address;
        this.tag=tag;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setTag(String tag){
      this.tag = tag;
    }

    public int getId() {

        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getName() {
        return name;
    }

    public String getTag(){
      return tag;
    }
}
