package org.vaadin.example;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;


/**
 *
 * @author Matti Tahvonen
 */
public class About extends Div {

    public About() {
        setText("A server side Tetris game, WTF!? ");
        add(new Anchor("https://github.com/mstahv/VaadinTetris", "GitHub"));
    }

}
