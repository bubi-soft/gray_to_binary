package sample;
/*FLOREA IONUT 342A3*/
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;

public class Main extends Application
{
    /* Definitii utile de dimensionare a elementelor */
    private static final int MAIN_WIDTH = 750;
    private static final int MAIN_HEIGHT = 600;
    private static final int LEFT_PADDING = 15;
    private static final int RIGHT_PADDING = 15;
    private static final int INTEROBJECT_DISTANCE = 10;

    /* Controale JavaFX */
    private Scene mainScene;

    private MenuBar toolbarMenu;
    private Menu fileMenu;
    private MenuItem openImageItem;
    private MenuItem saveImageItem;
    private SeparatorMenuItem separatorItem;
    private MenuItem closeItem;
    private Menu editMenu;
    private MenuItem setThresholdItem;
    private MenuItem convertItem;
    private Menu helpMenu;
    private MenuItem helpItem;

    private Button openImageButton;
    private Button saveImageButton;

    private Label thresholdLabel;
    private TextField thresholdField;
    private Slider thresholdSlider;
    private ToggleButton thresholdButton;

    private RadioButton automaticRadioButton;
    private RadioButton manualRadioButton;
    private Button convertButton;

    private TabPane imageTabPane;
    private Tab originalTab;
    private ImageView originalImageView;
    private Tab previewTab;
    private ImageView previewImageView;

    /* Variabile folosite intern */
    private String absoluteImagePath;
    private int threshold = 127;

    @Override
    public void start(Stage primaryStage)
    {

        Pane root = new Pane();
        root.setPrefWidth(MAIN_WIDTH);
        root.setPrefHeight(MAIN_HEIGHT);

        /* Creare meniu: Tool Bar */
        toolbarMenu = new MenuBar();
        toolbarMenu.setPrefWidth(MAIN_WIDTH + 20);

        /* Meniu Imagine */
        fileMenu = new Menu("Imagine");
        openImageItem = new MenuItem("Deschide Imagine");
        openImageItem.setOnAction(actionEvent -> openImage());
        saveImageItem = new MenuItem("Salveaza Imagine");
        saveImageItem.setOnAction(actionEvent -> saveImage());
        separatorItem = new SeparatorMenuItem();
        closeItem = new MenuItem("Inchide Aplicatia");
        closeItem.setOnAction(actionEvent -> Platform.exit());
        fileMenu.getItems().addAll(openImageItem, saveImageItem, separatorItem, closeItem);

        /* Meniu Editare */
        editMenu = new Menu("Editare");
        setThresholdItem = new MenuItem("Pragul conversiei");
        setThresholdItem.setOnAction(actionEvent -> updateMenuThreshold());
        convertItem = new MenuItem("Proceseaza Imaginea");
        convertItem.setOnAction(actionEvent -> processImage());
        editMenu.getItems().addAll(setThresholdItem, convertItem);

        /* Meniu Ajutor */
        helpMenu = new Menu("Ajutor");
        helpItem = new MenuItem("Ajutor");
        helpItem.setOnAction(actionEvent -> showHelpDialog());
        helpMenu.getItems().add(helpItem);

        toolbarMenu.getMenus().addAll(fileMenu, editMenu, helpMenu);
        root.getChildren().add(toolbarMenu);

        /* Butoane interfata */
        openImageButton = new Button("Incarca Imaginea");
        openImageButton.setOnAction(actionEvent -> openImage());
        openImageButton.setLayoutX(LEFT_PADDING);
        openImageButton.layoutYProperty().bind(toolbarMenu.heightProperty().add(INTEROBJECT_DISTANCE));
        saveImageButton = new Button("Salveaza Imaginea");
        saveImageButton.setOnAction(actionEvent -> saveImage());
        saveImageButton.layoutXProperty().bind(openImageButton.layoutXProperty().add(openImageButton.widthProperty().add(INTEROBJECT_DISTANCE)));
        saveImageButton.layoutYProperty().bind(toolbarMenu.heightProperty().add(INTEROBJECT_DISTANCE));

        root.getChildren().addAll(openImageButton, saveImageButton);

        /* Controlul pragului de conversie */

        thresholdLabel = new Label("Prag conversie: ");
        thresholdLabel.layoutXProperty().bind(saveImageButton.layoutXProperty().add(saveImageButton.widthProperty().add(INTEROBJECT_DISTANCE)));
        thresholdLabel.layoutYProperty().bind(toolbarMenu.heightProperty().add(INTEROBJECT_DISTANCE).add(saveImageButton.heightProperty().subtract(thresholdLabel.heightProperty()).divide(2)));

        thresholdField = new TextField("127");
        thresholdField.layoutXProperty().bind(thresholdLabel.layoutXProperty().add(thresholdLabel.widthProperty().add(INTEROBJECT_DISTANCE)));
        thresholdField.layoutYProperty().bind(toolbarMenu.heightProperty().add(INTEROBJECT_DISTANCE));
        thresholdField.setOnAction(actionEvent -> updateFieldThreshold());
        thresholdField.setEditable(false);

        thresholdSlider = new Slider(0, 255, 127);
        thresholdSlider.layoutXProperty().bind(thresholdField.layoutXProperty().add(thresholdField.widthProperty().add(INTEROBJECT_DISTANCE)));
        thresholdSlider.layoutYProperty().bind(toolbarMenu.heightProperty().add(INTEROBJECT_DISTANCE).add(thresholdField.heightProperty().subtract(thresholdSlider.heightProperty()).divide(2)));
        thresholdSlider.valueProperty().addListener(actionEvent -> updateSliderThreshold());

        thresholdButton = new ToggleButton("Slider");
        thresholdButton.layoutXProperty().bind(thresholdSlider.layoutXProperty().add(thresholdSlider.widthProperty().add(INTEROBJECT_DISTANCE)));
        thresholdButton.layoutYProperty().bind(toolbarMenu.heightProperty().add(INTEROBJECT_DISTANCE));
        thresholdButton.setSelected(true);
        thresholdButton.setOnAction(actionEvent -> changeThresholdInput());

        root.getChildren().addAll(thresholdLabel, thresholdField, thresholdSlider, thresholdButton);

        /* TabPane vizualizare imagine originala si procesata */

        imageTabPane = new TabPane();
        imageTabPane.setPrefWidth(MAIN_WIDTH);
        imageTabPane.setLayoutX(0);
        imageTabPane.layoutYProperty().bind(openImageButton.layoutYProperty().add(openImageButton.heightProperty().add(INTEROBJECT_DISTANCE)));
        imageTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);


        originalTab = new Tab("Imagine Orginala");
        originalImageView = new ImageView();
        originalImageView.setFitWidth(MAIN_WIDTH);
        originalImageView.fitHeightProperty().bind(imageTabPane.layoutYProperty().subtract(MAIN_HEIGHT).multiply(-1).subtract(imageTabPane.getTabMinHeight()).subtract(2 * INTEROBJECT_DISTANCE));
        originalTab.setContent(originalImageView);

        previewTab = new Tab("Imagine Procesata");
        previewImageView = new ImageView();
        previewImageView.setFitWidth(MAIN_WIDTH);
        previewImageView.fitHeightProperty().bind(imageTabPane.layoutYProperty().subtract(MAIN_HEIGHT).multiply(-1).subtract(imageTabPane.getTabMinHeight()).subtract(2 * INTEROBJECT_DISTANCE));
        previewTab.setContent(previewImageView);

        imageTabPane.getTabs().addAll(originalTab, previewTab);

        root.getChildren().add(imageTabPane);

        /* Alegerea metodei de selectare a pragului de conversie */

        automaticRadioButton = new RadioButton("Prag automat");
        automaticRadioButton.layoutXProperty().bind(automaticRadioButton.widthProperty().subtract(MAIN_WIDTH).add(RIGHT_PADDING).multiply(-1));
        automaticRadioButton.layoutYProperty().bind(openImageButton.layoutYProperty().add(openImageButton.heightProperty()).add(INTEROBJECT_DISTANCE).add(imageTabPane.tabMinHeightProperty().divide(4)));
        automaticRadioButton.setSelected(false);
        automaticRadioButton.setOnAction(actionEvent ->
        {
            manualRadioButton.setSelected(!manualRadioButton.isSelected());
            updateThresholdInputMethod();
        });

        manualRadioButton = new RadioButton("Prag manual");
        manualRadioButton.layoutXProperty().bind(automaticRadioButton.layoutXProperty().subtract(manualRadioButton.widthProperty()).subtract(INTEROBJECT_DISTANCE));
        manualRadioButton.layoutYProperty().bind(openImageButton.layoutYProperty().add(openImageButton.heightProperty()).add(INTEROBJECT_DISTANCE).add(imageTabPane.tabMinHeightProperty().divide(4)));
        manualRadioButton.setSelected(true);
        manualRadioButton.setOnAction(actionEvent ->
        {
            automaticRadioButton.setSelected(!automaticRadioButton.isSelected());
            updateThresholdInputMethod();
        });

        convertButton = new Button("Proceseaza Imaginea");
        convertButton.layoutXProperty().bind(manualRadioButton.layoutXProperty().subtract(convertButton.widthProperty()).subtract(INTEROBJECT_DISTANCE));
        convertButton.layoutYProperty().bind(openImageButton.layoutYProperty().add(openImageButton.heightProperty()).add(INTEROBJECT_DISTANCE));
        convertButton.setOnAction(actionEvent -> processImage());


        root.getChildren().addAll(automaticRadioButton, manualRadioButton, convertButton);

        mainScene = new Scene(root);
        primaryStage.setTitle("Convert Gray-Scale to Binary");
        primaryStage.setScene(mainScene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void showHelpDialog()
    {
        Alert helpDialog = new Alert(Alert.AlertType.INFORMATION);
        helpDialog.setTitle("Informatii Aplicatie");
        helpDialog.setHeaderText(null);

        /* Fereastra cu informatii despre aplicatie*/

        VBox helpBox = new VBox();

        Label helpLabel = new Label();
        helpLabel.setText("Aplicatie Java cu JavaFX UI - care sa realizeze o procesare deimagini.\n"+
                        "Conversia unei imagini in niveluri de gri intr-o imagine binara (Prag Static)\n"+
                        "\n"+
                        "Autor: Florea Ionut Grupa:342A3\n"+
                        "Version: 2.0\n"+
                        "\n"+
                        "Referinte utile:");

        Hyperlink tutorialHyperlink = new Hyperlink("JavaFX Tutorial");
        tutorialHyperlink.setOnAction(actionEvent ->openBrowser("http://www.java2s.com/Tutorials/Java/JavaFX/index.htm"));
        Hyperlink docsHyperlink = new Hyperlink("JavaFX Documentation");
        docsHyperlink.setOnAction(actionEvent ->openBrowser("https://docs.oracle.com/javase/8/javafx/api/"));
        Hyperlink binarizeHyperlink = new Hyperlink("Binarizarea unei imagini");
        binarizeHyperlink.setOnAction(actionEvent ->openBrowser("https://bostjan-cigan.com/articles/"));

        helpBox.getChildren().addAll(helpLabel,tutorialHyperlink,docsHyperlink,binarizeHyperlink);

        helpDialog.getDialogPane().setContent(helpBox);
        helpDialog.showAndWait();
    }

    private void openBrowser(String link)
    {
        if (java.awt.Desktop.isDesktopSupported())
        {
            try
            {
                java.awt.Desktop.getDesktop().browse(new URI(link));
            } catch (URISyntaxException exception)
            {
                exception.printStackTrace();
            } catch (IOException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    private void updateThresholdInputMethod()
    {
        if (automaticRadioButton.isSelected())
        {
            thresholdField.setDisable(true);
            thresholdSlider.setDisable(true);
            thresholdButton.setDisable(true);
            setThresholdItem.setDisable(true);
        }
        if (manualRadioButton.isSelected())
        {
            thresholdField.setDisable(false);
            thresholdSlider.setDisable(false);
            thresholdButton.setDisable(false);
            setThresholdItem.setDisable(false);
        }
    }

    private void changeThresholdInput()
    {
        if (thresholdButton.isSelected())
        {
            thresholdField.setEditable(false);
            thresholdButton.setText("Slider");
        } else
        {
            thresholdField.setEditable(true);
            thresholdButton.setText("TextField");
        }
    }

    private void updateSliderThreshold()
    {
        int userValue = (int) thresholdSlider.getValue();
        if (thresholdButton.isSelected())
        {
            checkUserThreshold(userValue);
            updateThresholdValue();
        } else
        {
            thresholdSlider.setValue(this.threshold);
        }
    }

    private void updateFieldThreshold()
    {
        try
        {
            int userValue = Integer.parseInt(thresholdField.getText());
            checkUserThreshold(userValue);
        } catch (Exception ex)
        {
            wrongValueError();
        }
        updateThresholdValue();
    }

    private void updateMenuThreshold()
    {
        TextInputDialog userInput = new TextInputDialog();
        userInput.showAndWait();
        int userValue;
        try
        {
            userValue = Integer.parseInt(userInput.getEditor().getText());
            checkUserThreshold(userValue);
            updateThresholdValue();
        } catch (Exception ex)
        {
            wrongValueError();
        }
    }

    private void wrongValueError()
    {
        Alert exAlert = new Alert(Alert.AlertType.ERROR);
        exAlert.setTitle("EROARE");
        exAlert.setHeaderText("Valoare introdusa nu este un numar");
        exAlert.setContentText(null);
        exAlert.showAndWait();
    }

    private boolean checkUserThreshold(int userValue)
    {
        if (0 <= userValue && userValue <= 255)
        {
            this.threshold = userValue;
            return true;
        } else
        {
            Alert exAlert = new Alert(Alert.AlertType.ERROR);
            exAlert.setTitle("EROARE");
            exAlert.setHeaderText("Valoare introdusa trebuie sa fie intre 0 si 255");
            exAlert.setContentText(null);
            exAlert.showAndWait();
            return false;
        }
    }

    private void updateThresholdValue()
    {
        this.thresholdField.setText(String.valueOf(this.threshold));
        this.thresholdSlider.setValue(this.threshold);
    }

    private void openImage()
    {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Alegeti imaginea");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("BMP files", "*.bmp"));
        File selectedImage = fileChooser.showOpenDialog(mainScene.getWindow());
        try
        {
            this.originalImageView.setImage(new javafx.scene.image.Image(new FileInputStream(selectedImage.getPath())));
            imageTabPane.getSelectionModel().select(originalTab);
            absoluteImagePath = selectedImage.getPath();
        } catch (NullPointerException ex)
        {
            noImageError();
        } catch (FileNotFoundException ex)
        {
            generalError(ex);
        }
    }

    private void saveImage()
    {
        if (absoluteImagePath == null)
        {
            noImageError();
        } else
        {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Alegeti imaginea");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("BMP files", "*.bmp"));
            File selectedImage = fileChooser.showSaveDialog(mainScene.getWindow());
            try
            {
                Binary.writeImage(selectedImage);
            } catch (IOException ex)
            {
                generalError(ex);
            } catch (IllegalArgumentException ex)
            {
                processImage();
                try
                {
                    Binary.writeImage(selectedImage);
                } catch (Exception ex1)
                {
                    generalError(ex);
                }
            }
        }
    }

    private void processImage()
    {
        try
        {
            Binary.setGrayscale(ImageIO.read(new FileInputStream(absoluteImagePath)));
            if(automaticRadioButton.isSelected())
            {
                this.threshold = Binary.automaticThreshold(Binary.getGrayscale());
                System.out.println(this.threshold);
                updateThresholdValue();
            }
            Binary.binarize(threshold);
            previewImageView.setImage(SwingFXUtils.toFXImage(Binary.getBinarized(), null));
            imageTabPane.getSelectionModel().select(previewTab);
        } catch (FileNotFoundException ex)
        {
            generalError(ex);
        } catch (IOException ex)
        {
            generalError(ex);
        } catch (NullPointerException ex)
        {
            noImageError();
        }
    }

    private void noImageError()
    {
        Alert exAlert = new Alert(Alert.AlertType.ERROR);
        exAlert.setTitle("EROARE");
        exAlert.setHeaderText("Nu ati incarcat nici o imagine");
        exAlert.setContentText(null);
        exAlert.showAndWait();
    }

    private void generalError(Exception ex)
    {

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exText = sw.toString();
        TextArea textArea = new TextArea(exText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        Alert exAlert = new Alert(Alert.AlertType.ERROR);
        exAlert.setTitle("EROARE");
        exAlert.setHeaderText(null);
        exAlert.getDialogPane().setContent(textArea);
        exAlert.showAndWait();
    }

    public static void main(String[] args)
    {
        launch(args);
    }
}
