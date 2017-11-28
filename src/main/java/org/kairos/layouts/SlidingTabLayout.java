package org.kairos.layouts;

import com.sun.javafx.scene.control.skin.ToggleButtonSkin;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.util.Duration;
import org.kairos.components.RippleSkinFactory;

/**
 * Created by Felipe on 04/11/2015.
 */
public class SlidingTabLayout extends VBox {

    private final int DEFAULT_HEIGHT_BAR = 3;

    private final HBox tabStrip = new HBox();
    private final VBox mainVBox = new VBox();
    public final HBox labelStrip = new HBox();

    private ViewPager viewPager;
    public Line bar = new Line();
    private Tab tabSelected;

    public SlidingTabLayout() {
        getStyleClass().add("sliding-tab-layout");
        bar.setManaged(false);
        bar.getStyleClass().add("bar");
        bar.setStrokeWidth(DEFAULT_HEIGHT_BAR);
        bar.layoutYProperty().bind(heightProperty().subtract(DEFAULT_HEIGHT_BAR - 1.5));

        labelStrip.prefHeightProperty().bind(prefHeightProperty().multiply(.35));
        labelStrip.setAlignment(Pos.CENTER);
        labelStrip.getStyleClass().add("sliding-tab-layout-labelStrip");

        tabStrip.setAlignment(Pos.CENTER);
        tabStrip.prefWidthProperty().bind(prefWidthProperty().subtract(4));

        mainVBox.setAlignment(Pos.BOTTOM_CENTER);
        mainVBox.getChildren().addAll(labelStrip, tabStrip);
        mainVBox.getStyleClass().add("sliding-tab-layout-mainVBox");

        getChildren().add(mainVBox);
        getChildren().add(bar);
    }

    public void setViewPager(ViewPager viewPager) {
        this.viewPager = viewPager;
        viewPager.setOnPageChangeListener(new PagerListener());
        populateStrip();
    }

    public Tab getCurrentTab() {
        return tabSelected;
    }

    private void populateStrip() {
        PagerAdapter adapter = viewPager.getAdapter();

        for (int i = 0; i < adapter.getCount(); i++) {
            Tab tab = new Tab();
            tab.setText(adapter.getPageTitle(i));

            if (adapter.getPageFile(i) != null) {
                try {
                    tab.styleProperty().bind(Bindings.concat("-fx-background-image: url(", adapter.getPageFile(i).toURI().toURL(), ");", "-fx-background-size: cover;"));
                } catch (MalformedURLException ex) {
                    Logger.getLogger(SlidingTabLayout.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            tab.setOnAction(evt -> {
                Tab tabPressed = (Tab) evt.getSource();
                viewPager.setCurrentItem(tabStrip.getChildren().indexOf(tabPressed));
                toggleButtonState(tabStrip.getChildren().indexOf(tabPressed));
            });
            if (i == 0) {
                tab.setSelected(true);
            }
            tab.prefWidthProperty().bind(tabStrip.prefWidthProperty());
            tabStrip.getChildren().add(tab);

            HBox hbox = new HBox();
            hbox.setAlignment(Pos.BOTTOM_CENTER);
            Label label = new Label();
            label.setText(adapter.getPageTitle(i));
            hbox.getChildren().add(label);
            HBox.setHgrow(hbox, Priority.ALWAYS);
            labelStrip.getChildren().add(hbox);
        }
    }

    private void toggleButtonState(int position) {
        PagerAdapter adapter = viewPager.getAdapter();

        for (int i = 0; i < adapter.getCount(); i++) {
            Tab tab = (Tab) tabStrip.getChildren().get(i);
            if (i == position) {
                tab.setSelected(true);
            } else {
                tab.setSelected(false);
            }
        }
    }

    private void animatingBar(double width, double x) {
        Timeline timeline = new Timeline(new KeyFrame(new Duration(200),
                new KeyValue(bar.endXProperty(), width - 20),
                new KeyValue(bar.layoutXProperty(), x + 10)
        ));
        timeline.play();

    }

    public class Tab extends ToggleButton {

        @Override
        protected Skin<?> createDefaultSkin() {
            SkinBase skin = new ToggleButtonSkin(this);
            RippleSkinFactory.getRippleEffect(skin, this);
            return super.createDefaultSkin();
        }

    }

    private class PagerListener implements ViewPager.OnPageChangeListener {

        @Override
        public void onPageSelected(int position) {

            tabSelected = (Tab) tabStrip.getChildren().get(position);
            if (tabSelected.getWidth() > 0) {
                animatingBar(tabSelected.getWidth(), getPadding().getLeft() + tabSelected.getLayoutX());
            } else {
                tabSelected.widthProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
                    if (newValue.intValue() > 0) {
                        animatingBar(tabSelected.getWidth(), getPadding().getLeft() + tabSelected.getLayoutX());
                    }
                });
            }
        }
    }
}
