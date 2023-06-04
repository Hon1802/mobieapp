package hcmute.edu.vn.finalprojectdemo.model;

public class Item   {
    private String resourceImage;
    private String txtImage;

    public Item() {
    }

    public Item(String resourceImage, String txtImage) {
        this.resourceImage = resourceImage;
        this.txtImage = txtImage;
    }

    public String getResourceImage() {
        return resourceImage;
    }

    public void setResourceImage(String resourceImage) {
        this.resourceImage = resourceImage;
    }

    public String getTxtImage() {
        return txtImage;
    }

    public void setTxtImage(String txtImage) {
        this.txtImage = txtImage;
    }
}
