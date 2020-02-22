package pl.wieczorkep._switch.server.menu;

import pl.wieczorkep._switch.server.view.View;


import java.util.*;

public class Menu {
    private final View view;

    public Menu(View view) {
        this.view = view;
    }

    public void show(List<?> choices) {
        if (choices.isEmpty()) {
            view.error("Empty menu.");
            return;
        }

        Iterator<?> choicesIterator = choices.iterator();
        for (int i = 1; choicesIterator.hasNext(); i++) {
            view.info(i + ". " + choicesIterator.next());
        }
    }

    public Object getChoice(List<?> choices) {
        int choice = view.readInt("Your choice");

        try {
            return choices.get(choice - 1);
        } catch (IndexOutOfBoundsException e) {
            view.error(e.getMessage());
            return null;
        }
    }
}
