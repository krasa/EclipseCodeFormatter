package krasa.formatter.settings;

import java.util.Objects;

public class GlobalProfileReference {
	private String name = null;
	private Long id = null;

	public GlobalProfileReference() {
	}

	public GlobalProfileReference(Long id, String name) {
		this.name = name;
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		GlobalProfileReference that = (GlobalProfileReference) o;
		return Objects.equals(name, that.name) && Objects.equals(id, that.id);
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, id);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("GlobalProfileReference{");
		sb.append("id=").append(id);
		sb.append(", name='").append(name).append('\'');
		sb.append('}');
		return sb.toString();
	}

}
