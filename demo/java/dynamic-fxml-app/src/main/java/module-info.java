module dev.omniui.demo.dynamicfxml {
    requires javafx.controls;
    requires javafx.fxml;
    requires dev.omniui.agent;
    opens dev.omniui.demo.dynamicfxml.controller to javafx.fxml;
    exports dev.omniui.demo.dynamicfxml;
}
