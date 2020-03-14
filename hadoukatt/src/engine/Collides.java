package engine;

/*
 * An interface that is implemented by any GameObject that
 * should be able to collide with other objects.
 */
public interface Collides {

	public void collision(GameObject go);
}
