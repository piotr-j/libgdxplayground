package io.piotrjastrzebski.playground.uitesting;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.VisWindow;
import io.piotrjastrzebski.playground.BaseScreen;
import io.piotrjastrzebski.playground.GameReset;
import io.piotrjastrzebski.playground.PLog;
import io.piotrjastrzebski.playground.PlaygroundGame;

/**
 *
 *
 *
 *
 */
public class UIInviteTest extends BaseScreen {
	private final static String TAG = UIInviteTest.class.getSimpleName();


	InviteManager invites;
	Array<Room> rooms;
	Array<Person> people;

	public UIInviteTest (GameReset game) {
		super(game);
		invites = new InviteManager();
		// we want direct invite + invite all
		rooms = new Array<>();
		for (int i = 0; i < 4; i++) {
			Room room = new Room(i, "Room " + (i + 1));
			rooms.add(room);
		}

		people = new Array<>();
		{
			Table table = new Table();
			root.add(table).row();

			for (int i = 0; i < 5; i++) {
				Person person = new Person(i, "Person " + (i + 1), rooms, invites);
				people.add(person);
				table.add(person.view()).pad(16);
			}
		}
		{
			Table table = new Table();
			root.add(table).grow();

			for (Room room : rooms) {
				table.add(room).pad(16);
			}
		}
	}



	@Override public void render (float delta) {
		super.render(delta);
		stage.act(delta);
		stage.draw();

	}

	static class Person {
		int id;
		String name;
		Array<Room> rooms;
		InviteManager invites;
		Array<View> views = new Array<>();

		public Person (int id, String name, Array<Room> rooms, InviteManager invites) {
			this.id = id;
			this.name = name;
			this.rooms = rooms;
			this.invites = invites;
		}

		public Actor view () {
			View view = new View(this);
			views.add(view);
			return view;
		}

		public void inviteFrom (final Room room) {
			VisDialog dialog = new VisDialog("Invite " + room.name);
			dialog.addCloseButton();
			Table content = dialog.getContentTable();
			for (final Person person : room.people) {
				if (person.id == id) continue;
				content.add(new VisLabel(person.name));
				final VisTextButton button = new VisTextButton("??");
				content.add(button).width(100).row();
				if (invites.isInvited(this, person)) {
					button.setText("Cancel");
				} else {
					button.setText("Invite");
				}
				button.addListener(new ChangeListener() {
					@Override public void changed (ChangeEvent event, Actor actor) {
						if (invites.isInvited(Person.this, person)) {
							invites.cancel(Person.this, person);
							button.setText("Invite");
						} else {
							invites.invite(Person.this, person, room);
							button.setText("Cancel");
						}
					}
				});
			}

			final VisTextButton button = new VisTextButton("Invite all");
			content.add(button).width(100).row();
			button.addListener(new ChangeListener() {
				@Override public void changed (ChangeEvent event, Actor actor) {
					for (Person person : room.people) {
						if (person.id == id) continue;
						if (invites.isInvited(Person.this, person)) {
							invites.cancel(Person.this, person);
						}
						invites.invite(Person.this, person, room);

					}
				}
			});

			dialog.show(room.getStage());
		}

		static class View extends Table {
			VisLabel name;
			Person person;
			Table content;

			public View (final Person person) {
				this.person = person;
				name = new VisLabel(person.name);
				add(name).row();

				person.views.add(this);

				content = new Table();
				add(content);

				refresh();
			}

			void refresh () {
				content.clear();
				for (final Room r : person.rooms) {
					content.add(new VisLabel(r.name)).growX().padRight(8);
					final VisTextButton button = new VisTextButton("Enter");
					content.add(button).width(100).row();
					button.addListener(new ChangeListener() {
						@Override public void changed (ChangeEvent event, Actor actor) {
							if (r.entered(person)) {
								r.leave(person);
								button.setText("Enter");
							} else {
								r.enter(person);
								button.setText("Leave");
							}
						}
					});
				}
				final VisTextButton button = new VisTextButton("Leave all");
				content.add().growX();
				content.add(button).width(100).row();
				button.addListener(new ChangeListener() {
					@Override public void changed (ChangeEvent event, Actor actor) {
						for (Room r : person.rooms) {
							r.leave(person);
						}
						refresh();
					}
				});
			}
		}

		@Override public String toString () {
			return "Person{" + "name='" + name + '\'' + '}';
		}
	}

	static class Room extends VisWindow {
		int id;
		String name;
		Array<Person> people = new Array<>();
		Table peopleContainer;

		public Room (int id, String name) {
			super(name);
			this.id = id;
			this.name = name;
			peopleContainer = new Table();
			add(peopleContainer).row();
			setMovable(false);
		}

		boolean entered (Person person) {
			return people.contains(person, true);
		}

		boolean enter (Person person) {
			if (entered(person)) return false;
			people.add(person);
			rebuildPeople();
			return true;
		}

		boolean leave (Person person) {
			if (people.removeValue(person, true)) {
				rebuildPeople();
				return true;
			}
			return false;
		}

		private void rebuildPeople () {
			peopleContainer.clear();
			for (final Person person : people) {
				peopleContainer.add(new VisLabel(person.name));

				final VisTextButton button = new VisTextButton("Invites...");
				peopleContainer.add(button).width(100).row();
				button.addListener(new ChangeListener() {
					@Override public void changed (ChangeEvent event, Actor actor) {
						person.inviteFrom(Room.this);
					}
				});
			}
		}

		@Override public String toString () {
			return "Room{" + "name='" + name + '\'' + '}';
		}
	}


	static class InviteManager {
		ObjectMap<InviteKey, InviteData> invites;

		public InviteManager () {
			this.invites = new ObjectMap<>();
		}

		public boolean isInvited (Person p1, Person p2) {
			return invites.containsKey(new InviteKey(p1, p2));
		}

		public void invite (Person p1, Person p2, Room room) {
			// make sure we dont have other pending first
			cancel(p1, p2);
			PLog.log(p1 + " invites " + p2 + " in " + room);
			invites.put(new InviteKey(p1, p2), new InviteData(p1, p2, room));
		}

		public void cancel (Person p1, Person p2) {
			InviteData data = invites.remove(new InviteKey(p1, p2));
			if (data != null) {
				PLog.log(p1 + " cancels " + p2 + " invite from " + data.room);
				// notify?
			}
		}

		static class InviteKey {
			final Person p1;
			final Person p2;

			public InviteKey (Person p1, Person p2) {
				if (p1.id < p2.id) {
					this.p1 = p1;
					this.p2 = p2;
				} else {
					this.p1 = p2;
					this.p2 = p1;
				}
			}

			@Override public boolean equals (Object o) {
				if (this == o)
					return true;
				if (o == null || getClass() != o.getClass())
					return false;

				InviteKey inviteKey = (InviteKey)o;

				if (!p1.equals(inviteKey.p1))
					return false;
				return p2.equals(inviteKey.p2);
			}

			@Override public int hashCode () {
				int result = p1.hashCode();
				result = 31 * result + p2.hashCode();
				return result;
			}
		}

		static class InviteData {
			Person source;
			Person target;
			Room room;

			public InviteData (Person source, Person target, Room room) {
				this.source = source;
				this.target = target;
				this.room = room;
			}
		}
	}

	public static void main (String[] args) {
		PlaygroundGame.start(args, UIInviteTest.class);
	}

}
