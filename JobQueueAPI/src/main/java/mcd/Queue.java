package mcd;


/**
 *
 * @author pierre.coppee
 */
public class Queue {

    private String name;
    private String description;
    private Integer slot;


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getSlot() {
        return slot;
    }

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}

	/**
	 * @param slot the slot to set
	 */
	public void setSlot(int slot)
	{
		this.slot = slot;
	}

}
