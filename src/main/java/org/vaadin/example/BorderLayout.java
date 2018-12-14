package org.vaadin.example;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;

@HtmlImport("frontend://custom-style.html")
public class BorderLayout extends Div {

    public BorderLayout() {
        super();
        addClassName("border-layout");
    }

    public void addNorth(HasStyle component) {
        component.addClassName("north");
        add((Component) component);
    }

    public void addSouth(HasStyle component) {
        component.addClassName("south");
        add((Component) component);
    }

    public void addEast(HasStyle component) {
        component.addClassName("east");
        add((Component) component);
    }

    public void addWest(HasStyle component) {
        component.addClassName("west");
        add((Component) component);
    }
}
