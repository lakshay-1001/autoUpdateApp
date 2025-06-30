package com.shop;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;

import com.shop.utils.ConfigUtil;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SelfUpdater {
    private static final String version = (ConfigUtil.getProperty("version"));
    private static final String installerPath = (ConfigUtil.getProperty("target.path"));
    private static final String PREVIOUS_VERSION = version;
    private static final Logger logger = Logger.getLogger(SelfUpdater.class.getName());
    public static void main(String[] args) {
        try {
            
        	logger.info(version);

            JSONObject updateInfo = UpdateChecker.fetchUpdateInfo();
            String latestVersion = updateInfo.getString("version");
            String downloadUrl = updateInfo.getString("downloadUrl");

            if (UpdateChecker.isUpdateAvailable(PREVIOUS_VERSION, latestVersion)) {
            	logger.info("Update available!");
                showMaintenanceStage(downloadUrl, latestVersion, installerPath);

            } else {
            	logger.info("Application is up-to-date.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Failed to check for updates.");
        }
    }

    public static void replaceOldApplication(String sourcePath, String targetPath) throws IOException {
        File targetFile = new File(targetPath);

        targetFile.getParentFile().mkdirs();

        Files.copy(Paths.get(sourcePath), Paths.get(targetPath), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        logger.info("Application updated successfully!");
    }

    public static void runInstaller(String installerPath) {
        File installer = new File(installerPath);
        if (installer.exists()) {
            // Launch the installer
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", installer.getAbsolutePath());
            try {
				processBuilder.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
            logger.info("Installer launched.");
        } else {
        	logger.info("Installer file not found.");
        }
    }

    public static void showMaintenanceStage(String downloadUrl, String latestVersion, String installationDirectory) {
        Platform.runLater(() -> {
            Stage stage = createMaintenanceStage();
            stage.setFullScreen(true);

            AnchorPane anchorPane = new AnchorPane();

            ImageView backgroundImageView = new ImageView(
                    new Image(SelfUpdater.class.getResource("/com/shop/images/bg-banner.jpg").toExternalForm())
            );
            backgroundImageView.setPreserveRatio(false);
            backgroundImageView.fitWidthProperty().bind(anchorPane.widthProperty());
            backgroundImageView.fitHeightProperty().bind(anchorPane.heightProperty());
            AnchorPane.setTopAnchor(backgroundImageView, 0.0);
            AnchorPane.setLeftAnchor(backgroundImageView, 0.0);
            AnchorPane.setBottomAnchor(backgroundImageView, 0.0);
            AnchorPane.setRightAnchor(backgroundImageView, 0.0);

            // Loader container
            StackPane loaderContainer = new StackPane();

            // Rotating images
            ImageView rotatingImageView = new ImageView(
                    new Image(SelfUpdater.class.getResource("/com/shop/images/loader.png").toExternalForm())
            );
            rotatingImageView.setFitWidth(145);
            rotatingImageView.setFitHeight(139);

            ImageView rotatingImageViewRing = new ImageView(
                    new Image(SelfUpdater.class.getResource("/com/shop/images/loader-ring.png").toExternalForm())
            );
            rotatingImageViewRing.setFitWidth(308);
            rotatingImageViewRing.setFitHeight(308);

            loaderContainer.getChildren().addAll(rotatingImageViewRing, rotatingImageView);
            loaderContainer.setAlignment(Pos.CENTER);

            // Rotate animation
            RotateTransition rotateTransition = new RotateTransition(Duration.seconds(2), loaderContainer);
            rotateTransition.setByAngle(360);
            rotateTransition.setCycleCount(Animation.INDEFINITE);
            rotateTransition.setInterpolator(Interpolator.LINEAR);
            rotateTransition.play();

            Label progressLabel = new Label();
            progressLabel.setAlignment(Pos.CENTER);
            progressLabel.setTextAlignment(TextAlignment.CENTER);
            progressLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: #ffffff; -fx-font-weight: Bold;");

            // Dynamic message
            Label messageLabel = new Label("Services are Under Maintainance"+"\nPlease wait!");
            messageLabel.setAlignment(Pos.CENTER);
            messageLabel.setTextAlignment(TextAlignment.CENTER);
            messageLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: #ffffff; -fx-font-weight: Bold;");

            // Layout for loader and message
            VBox loaderLayout = new VBox(10, loaderContainer, progressLabel, messageLabel);
            loaderLayout.setStyle("-fx-background-color: rgba(19, 66, 60, 0.92);");
            loaderLayout.setAlignment(Pos.CENTER);

            AnchorPane.setTopAnchor(loaderLayout, 0.0);
            AnchorPane.setLeftAnchor(loaderLayout, 0.0);
            AnchorPane.setBottomAnchor(loaderLayout, 0.0);
            AnchorPane.setRightAnchor(loaderLayout, 0.0);

            anchorPane.getChildren().addAll(backgroundImageView, loaderLayout);

            Scene scene = new Scene(anchorPane);
            stage.setScene(scene);
            stage.setFullScreenExitHint("");
            stage.setFullScreen(true);

            stage.show();

            String[] messages = {"Please wait...", "Services are under Maintainance"};
            AtomicInteger messageIndex = new AtomicInteger(0);


            Timeline messageTimeline = new Timeline(new KeyFrame(Duration.seconds(3), event -> 
                Platform.runLater(() -> {
                    // Create fade-out animation
                    FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), messageLabel);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);

                    fadeOut.setOnFinished(fadeOutEvent -> {
                        // Change the message once faded out
                        messageLabel.setText(
                                messages[messageIndex.getAndIncrement() % messages.length]
                        );

                        // Create fade-in animation
                        FadeTransition fadeIn = new FadeTransition(Duration.seconds(0.5), messageLabel);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                    });

                    fadeOut.play();
                })
            ));

            messageTimeline.setCycleCount(Animation.INDEFINITE);
            Platform.runLater(messageTimeline::play);

            new Thread(() -> {
                try {
                    String tempPath = System.getenv("TEMP") + File.separator + "TempleStreet_" +latestVersion+ ".exe";
                    UpdateDownloader.downloadUpdate(downloadUrl, tempPath, progress -> Platform.runLater(() -> {
                        progressLabel.setText("Downloading Update: " + progress + "%\n");
                        messageLabel.setText(
                                messages[messageIndex.get() % messages.length]
                        );
                    }));

                    Platform.runLater(() -> {
                        messageTimeline.stop();
                        progressLabel.setVisible(false);
                        progressLabel.setManaged(false);
                        messageLabel.setText("Relaunching Application. Please Wait....");
                    });

                    String targetPath = installerPath +"TempleStreet_" +latestVersion+ ".exe";

                    deletePreviousVersions(installationDirectory, "1.0.0");

                    replaceOldApplication(tempPath, targetPath);

                    updateDesktopShortcut("TempleStreet", targetPath);

                    updateProgramPath("TempleStreet", targetPath);

                    runInstaller(targetPath);
                    System.exit(0);

                } catch (Exception e) {
                    e.printStackTrace();
                    Platform.runLater(() -> {
                        messageLabel.setText("Update failed. Please try again later.");
                        stage.close();
                    });
                }
            }).start();
        });
    }

    private static Stage createMaintenanceStage() {
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        return stage;
    }

    public static void updateDesktopShortcut(String appName, String targetPath) throws IOException {
        String publicDesktopPath = System.getenv("PUBLIC") + "\\Desktop";
        String shortcutPath = publicDesktopPath + "\\" + appName + ".lnk";
        String commondStr = "-Command";
        String noProfile =  "-NoProfile";

        File oldShortcut = new File(shortcutPath);
        if (oldShortcut.exists()) {
        	logger.info("Old shortcut found: " + shortcutPath);

            if (oldShortcut.delete()) {
            	logger.info("Old shortcut deleted successfully using Java.");
            } else {
            	logger.severe("Failed to delete old shortcut using Java. Attempting PowerShell deletion...");

                String deleteScript = String.format("Remove-Item -Path '%s' -Force", shortcutPath);
                ProcessBuilder deleteProcessBuilder = new ProcessBuilder(
                        "powershell.exe", noProfile, commondStr, deleteScript
                );
                deleteProcessBuilder.inheritIO();
                Process deleteProcess = deleteProcessBuilder.start();
                try {
                    int deleteExitCode = deleteProcess.waitFor();
                    if (deleteExitCode != 0) {
                    	logger.severe("Failed to delete old shortcut via PowerShell. Exit code: " + deleteExitCode);
                    } else {
                    	logger.severe("Old shortcut deleted successfully via PowerShell.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Shortcut deletion process was interrupted.", e);
                }
            }
        } else {
        	logger.info("No existing shortcut found to delete.");
        }

        logger.info("Creating new shortcut at: " + shortcutPath);
        String script = String.format(
                "$WScriptShell = New-Object -ComObject WScript.Shell;" +
                        "$Shortcut = $WScriptShell.CreateShortcut('%s');" +
                        "$Shortcut.TargetPath = '%s';" +
                        "$Shortcut.IconLocation = '%s';" +
                        "$Shortcut.Save();",
                shortcutPath.replace("\\", "\\\\"), // Escape backslashes
                targetPath.replace("\\", "\\\\"),
                targetPath.replace("\\", "\\\\") // Use the app executable as the icon
        );

        ProcessBuilder createProcessBuilder = new ProcessBuilder(
                "powershell.exe", noProfile, commondStr, script
        );
        createProcessBuilder.inheritIO();
        Process createProcess = createProcessBuilder.start();

        try {
            int createExitCode = createProcess.waitFor();
            if (createExitCode == 0) {
            	logger.info("Desktop shortcut replaced successfully!");
            } else {
                throw new IOException("Failed to create new desktop shortcut. Exit code: " + createExitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Shortcut creation process was interrupted.", e);
        }
    }

    public static void updateProgramPath(String appName, String targetPath) throws IOException {
        String programPath = System.getenv("ProgramData") + "\\Microsoft\\Windows\\Start Menu\\Programs";
        String shortcutPath = programPath + "\\" + appName + ".lnk";
        String commondStr = "-Command";
        String noProfile =  "-NoProfile";

        File oldShortcut = new File(shortcutPath);
        if (oldShortcut.exists()) {
        	logger.info("Old shortcut found: " + shortcutPath);

            if (oldShortcut.delete()) {
            	logger.info("Old shortcut deleted successfully using Java.");
            } else {
            	logger.severe("Failed to delete old shortcut using Java. Attempting PowerShell deletion...");

                String deleteScript = String.format("Remove-Item -Path '%s' -Force", shortcutPath);
                ProcessBuilder deleteProcessBuilder = new ProcessBuilder(
                        "powershell.exe", noProfile, commondStr, deleteScript
                );
                deleteProcessBuilder.inheritIO();
                Process deleteProcess = deleteProcessBuilder.start();
                try {
                    int deleteExitCode = deleteProcess.waitFor();
                    if (deleteExitCode != 0) {
                    	logger.severe("Failed to delete old shortcut via PowerShell.: " + deleteExitCode);
                    } else {
                    	logger.info("Old shortcut deleted successfully via PowerShell.");
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Shortcut deletion process was interrupted.", e);
                }
            }
        } else {
        	logger.info("No existing shortcut found to delete.");
        }

        logger.info("Creating new shortcut at: " + shortcutPath);
        String script = String.format(
                "$WScriptShell = New-Object -ComObject WScript.Shell;" +
                        "$Shortcut = $WScriptShell.CreateShortcut('%s');" +
                        "$Shortcut.TargetPath = '%s';" +
                        "$Shortcut.IconLocation = '%s';" +
                        "$Shortcut.Save();",
                shortcutPath.replace("\\", "\\\\"), // Escape backslashes
                targetPath.replace("\\", "\\\\"),
                targetPath.replace("\\", "\\\\") // Use the app executable as the icon
        );

        ProcessBuilder createProcessBuilder = new ProcessBuilder(
                "powershell.exe", noProfile, commondStr, script
        );
        createProcessBuilder.inheritIO();
        Process createProcess = createProcessBuilder.start();

        try {
            int createExitCode = createProcess.waitFor();
            if (createExitCode == 0) {
            	logger.info("Desktop shortcut replaced successfully!");
            } else {
                throw new IOException("Failed to create new desktop shortcut. Exit code: " + createExitCode);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Shortcut creation process was interrupted.", e);
        }
    }


    public static void deletePreviousVersions(String installationDirectory, String currentVersion) {
    	File directory = new File(installationDirectory);

        if (!directory.isDirectory()) {
        	logger.severe("Invalid installation directory: " + installationDirectory);
            return;
        }

        File[] files = directory.listFiles((dir, name) -> name.matches("TempleStreet_\\d+\\.\\d+\\.\\d+\\.exe"));

        if (files == null) return;

        Pattern pattern = Pattern.compile("TempleStreet_(\\d+\\.\\d+\\.\\d+)\\.exe");

        for (File file : files) {
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.matches() && !matcher.group(1).equals(currentVersion)) {
                if (file.delete()) {
                	logger.info("Deleted: " + file.getName());
                } else {
                	logger.severe("Failed to delete: " + file.getName());
                }
            }
        }
    }


}
