package com.audiolab.ui;

import com.audiolab.i18n.I18n;
import com.audiolab.io.SupportedAudioFormats;
import com.audiolab.model.AudioMetadata;
import com.audiolab.model.CompressionAlgorithm;
import com.audiolab.model.CompressionReport;
import com.audiolab.model.CompressionSettings;
import com.audiolab.model.ProcessingState;
import com.audiolab.model.AudioSession;
import com.audiolab.service.AudioExportService;
import com.audiolab.service.AudioIOService;
import com.audiolab.service.AudioPlaybackService;
import com.audiolab.service.CompressionService;
import com.audiolab.service.PerformanceMonitor;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.NodeOrientation;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {

    @FXML private BorderPane root;
    @FXML private Label appLogoLabel;
    @FXML private Button btnOpen;
    @FXML private Button btnPlay;
    @FXML private Button btnCompress;
    @FXML private Button btnDecompress;
    @FXML private Button btnSave;
    @FXML private Button btnReset;
    @FXML private Button btnOpenEmpty;
    @FXML private Button btnCancel;
    @FXML private Menu menuFile;
    @FXML private MenuItem menuOpen;
    @FXML private MenuItem menuSave;
    @FXML private MenuItem menuReset;
    @FXML private MenuItem menuExit;
    @FXML private StackPane workspacePane;
    @FXML private StackPane waveformHost;
    @FXML private VBox workspaceEmpty;
    @FXML private Label emptyTitleLabel;
    @FXML private Label emptySubtitleLabel;
    @FXML private ProgressBar progressBar;
    @FXML private LineChart<Number, Number> ratioChart;
    @FXML private LineChart<Number, Number> speedChart;
    @FXML private NumberAxis ratioTimeAxis;
    @FXML private NumberAxis ratioValueAxis;
    @FXML private NumberAxis speedTimeAxis;
    @FXML private NumberAxis speedValueAxis;
    @FXML private ScrollPane sidebarScroll;
    @FXML private Label sidebarTitleLabel;
    @FXML private Label sidebarSectionMetadata;
    @FXML private Label metaKeyName;
    @FXML private Label metaKeySize;
    @FXML private Label metaKeyDuration;
    @FXML private Label metaKeySampleRate;
    @FXML private Label metaKeyChannels;
    @FXML private Label metaKeyBitRate;
    @FXML private Label metaKeyEncoding;
    @FXML private Label metaNameLabel;
    @FXML private Label metaSizeLabel;
    @FXML private Label metaDurationLabel;
    @FXML private Label metaSampleRateLabel;
    @FXML private Label metaChannelsLabel;
    @FXML private Label metaBitRateLabel;
    @FXML private Label metaEncodingLabel;
    @FXML private VBox optionsPlaceholder;
    @FXML private Label optionsPlaceholderTitle;
    @FXML private Label optionsPlaceholderText;
    @FXML private VBox settingsCard;
    @FXML private Label sidebarSectionSettings;
    @FXML private Label settingsAlgorithmLabel;
    @FXML private ComboBox<CompressionAlgorithm> algorithmCombo;
    @FXML private Label settingsSampleRateLabel;
    @FXML private Spinner<Integer> sampleRateSpinner;
    @FXML private Label settingsQuantizationLabel;
    @FXML private Spinner<Integer> quantizationSpinner;
    @FXML private Label settingsStepSizeLabel;
    @FXML private Spinner<Integer> stepSizeSpinner;
    @FXML private Label settingsInitialStepLabel;
    @FXML private Spinner<Integer> initialStepSpinner;
    @FXML private Label settingsMinStepLabel;
    @FXML private Spinner<Integer> minStepSpinner;
    @FXML private Label settingsMaxStepLabel;
    @FXML private Spinner<Integer> maxStepSpinner;
    @FXML private Label settingsAdaptationLabel;
    @FXML private Spinner<Double> adaptationSpinner;
    @FXML private VBox reportCard;
    @FXML private Label sidebarSectionReport;
    @FXML private Label reportEmptyLabel;
    @FXML private VBox reportContent;
    @FXML private Label reportKeyOriginal;
    @FXML private Label reportKeyCompressed;
    @FXML private Label reportKeySavings;
    @FXML private Label reportKeyElapsed;
    @FXML private Label reportKeyAlgorithm;
    @FXML private Label reportKeySettings;
    @FXML private Label reportKeyAvgSpeed;
    @FXML private Label reportKeyPeakSpeed;
    @FXML private Label reportOriginalLabel;
    @FXML private Label reportCompressedLabel;
    @FXML private Label reportSavingsLabel;
    @FXML private Label reportElapsedLabel;
    @FXML private Label reportAlgorithmLabel;
    @FXML private Label reportSettingsLabel;
    @FXML private Label reportAvgSpeedLabel;
    @FXML private Label reportPeakSpeedLabel;
    @FXML private StackPane statusDotPane;
    @FXML private ProgressIndicator statusSpinner;
    @FXML private Label statusLabel;
    @FXML private Label shortcutsLabel;

    private Stage primaryStage;
    private final AudioSession session = new AudioSession();
    private final AudioIOService ioService = new AudioIOService();
    private final AudioExportService exportService = new AudioExportService();
    private final AudioPlaybackService playbackService = new AudioPlaybackService();
    private final CompressionService compressionService = new CompressionService();
    private final PerformanceMonitor monitor = new PerformanceMonitor();
    private final ExecutorService loader = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "audiolab-loader");
        t.setDaemon(true);
        return t;
    });

    private WaveformView waveformView;
    private XYChart.Series<Number, Number> ratioSeries;
    private XYChart.Series<Number, Number> speedSeries;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        applyDirection(Locale.getDefault());
        bindLocalizedTexts();
        setupWaveform();
        setupCharts();
        setupSpinners();
        setupAlgorithmCombo();
        setupDragAndDrop();
        setupKeyboardShortcuts();
        setupPlaybackListener();
        bindMonitor();
        bindSession();

        setStatus(I18n.get("status.ready"), StatusMode.IDLE);
        clearMetadataLabels();
        showOptionsForAudio(false);
        updateWorkspaceEmpty(true);
        setActionButtonsEnabled(false);
    }

    private void setupWaveform() {
        waveformView = new WaveformView();
        StackPane.setAlignment(waveformView, Pos.CENTER);
        waveformHost.getChildren().setAll(waveformView);
    }

    private void setupCharts() {
        ratioChart.setLegendVisible(false);
        speedChart.setLegendVisible(false);
        ratioChart.setCreateSymbols(false);
        speedChart.setCreateSymbols(false);

        ratioSeries = new XYChart.Series<>();
        speedSeries = new XYChart.Series<>();
        ratioSeries.setData(monitor.ratioSeries());
        speedSeries.setData(monitor.speedSeries());
        ratioChart.getData().add(ratioSeries);
        speedChart.getData().add(speedSeries);

        ratioValueAxis.setAutoRanging(true);
        ratioValueAxis.setLowerBound(0);
        speedValueAxis.setAutoRanging(true);
        speedValueAxis.setLowerBound(0);
        ratioTimeAxis.setAutoRanging(true);
        speedTimeAxis.setAutoRanging(true);
    }

    private void setupSpinners() {
        sampleRateSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8000, 96000, 44100, 1000));
        quantizationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(4, 256, 64, 4));
        stepSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(16, 8192, 512, 16));
        initialStepSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(16, 8192, 512, 16));
        minStepSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 4096, 64, 8));
        maxStepSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(64, 16384, 4096, 64));
        adaptationSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.1, 3.0, 1.5, 0.1));

        algorithmCombo.valueProperty().addListener((obs, oldVal, algorithm) -> updateSettingsVisibility(algorithm));
    }

    private void setupAlgorithmCombo() {
        algorithmCombo.getItems().setAll(CompressionAlgorithm.values());
        algorithmCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(CompressionAlgorithm algorithm) {
                return algorithm == null ? "" : I18n.get(algorithm.i18nKey());
            }

            @Override
            public CompressionAlgorithm fromString(String string) {
                return null;
            }
        });
        algorithmCombo.getSelectionModel().select(CompressionAlgorithm.DPCM);
    }

    private void setupPlaybackListener() {
        playbackService.setPlayingStateListener(playing -> Platform.runLater(() -> {
            btnPlay.setText(I18n.get(playing ? "action.stop" : "action.play"));
            if (playing) {
                setStatus(I18n.get("status.playing"), StatusMode.BUSY);
            } else if (session.hasAudio()) {
                setStatus(I18n.get("status.loaded", session.metadata().map(AudioMetadata::fileName).orElse("")), StatusMode.IDLE);
            }
        }));
    }

    private void bindMonitor() {
        progressBar.progressProperty().bind(monitor.progressProperty());
    }

    private void bindSession() {
        session.lastReportProperty().addListener((obs, oldReport, report) -> Platform.runLater(() -> updateReport(report)));
        session.processingStateProperty().addListener((obs, oldState, state) -> Platform.runLater(() -> updateProcessingUi(state)));
    }

    private void setupDragAndDrop() {
        root.setOnDragOver(this::handleDragOver);
        root.setOnDragDropped(this::handleDragDropped);
    }

    private void handleDragOver(DragEvent event) {
        Dragboard db = event.getDragboard();
        if (db.hasFiles() && db.getFiles().stream().anyMatch(this::isSupportedAudio)) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;
        if (db.hasFiles()) {
            for (File file : db.getFiles()) {
                if (isSupportedAudio(file)) {
                    loadAudioFile(file);
                    success = true;
                    break;
                }
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void setupKeyboardShortcuts() {
        root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.O && event.isShortcutDown()) {
                onOpenAudio();
                event.consume();
            } else if (event.getCode() == KeyCode.S && event.isShortcutDown()) {
                onSave();
                event.consume();
            } else if (event.getCode() == KeyCode.R && event.isShortcutDown() && event.isShiftDown()) {
                onReset();
                event.consume();
            }
        });
    }

    @FXML
    private void onOpenAudio() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("dialog.open.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.get("dialog.filter.audio"), SupportedAudioFormats.importGlobs()));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.get("dialog.filter.audc"), "*.audc", "*.AUDC"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.get("dialog.filter.allSupported"), SupportedAudioFormats.allGlobs()));
        File file = chooser.showOpenDialog(primaryStage);
        if (file != null) {
            loadAudioFile(file);
        }
    }

    private void loadAudioFile(File file) {
        playbackService.stop();
        setStatus(I18n.get("status.loading"), StatusMode.BUSY);
        loader.submit(() -> {
            try {
                if (SupportedAudioFormats.isAudc(file)) {
                    var loaded = ioService.loadAudc(file);
                    Platform.runLater(() -> applyLoadedAudc(file, loaded));
                } else {
                    var loaded = ioService.load(file);
                    Platform.runLater(() -> applyLoadedAudio(file, loaded));
                }
            } catch (Exception e) {
                Platform.runLater(() -> showError(e.getMessage()));
            }
        });
    }

    private void applyLoadedAudio(File file, AudioIOService.LoadedAudio loaded) {
        session.open(file, loaded.metadata(), loaded.samples());
        CompressionSettings settings = session.settings();
        settings.applyDefaultsFromMetadata(loaded.metadata());
        syncSettingsToUi(settings);

        updateMetadata(loaded.metadata());
        waveformView.setSamples(loaded.samples(), loaded.metadata().channels());
        monitor.setOriginalSizeBytes(loaded.metadata().fileSizeBytes());
        monitor.reset();
        ratioSeries.getData().clear();
        speedSeries.getData().clear();

        showOptionsForAudio(true);
        updateWorkspaceEmpty(false);
        refreshActionButtons();
        setStatus(I18n.get("status.loaded", loaded.metadata().fileName()), StatusMode.IDLE);
    }

    private void applyLoadedAudc(File file, AudioIOService.LoadedAudc loaded) {
        var header = loaded.parsed().header();
        session.openFromAudc(file, loaded.metadata(), loaded.containerBytes(),
                loaded.parsed().payload(), header.settings());

        syncSettingsToUi(session.settings());
        updateMetadata(loaded.metadata());
        waveformView.clear();
        monitor.setOriginalSizeBytes((long) header.sampleCount() * 2L);
        monitor.reset();
        ratioSeries.getData().clear();
        speedSeries.getData().clear();

        showOptionsForAudio(true);
        updateWorkspaceEmpty(false);
        refreshActionButtons();
        setStatus(I18n.get("status.loadedAudc", loaded.metadata().fileName()), StatusMode.IDLE);
    }

    private void syncSettingsToUi(CompressionSettings settings) {
        algorithmCombo.getSelectionModel().select(settings.getAlgorithm());
        sampleRateSpinner.getValueFactory().setValue(settings.getTargetSampleRate());
        quantizationSpinner.getValueFactory().setValue(settings.getQuantizationLevels());
        stepSizeSpinner.getValueFactory().setValue(settings.getStepSize());
        initialStepSpinner.getValueFactory().setValue(settings.getInitialStepSize());
        minStepSpinner.getValueFactory().setValue(settings.getMinStepSize());
        maxStepSpinner.getValueFactory().setValue(settings.getMaxStepSize());
        adaptationSpinner.getValueFactory().setValue(settings.getAdaptationFactor());
        updateSettingsVisibility(settings.getAlgorithm());
    }

    private CompressionSettings readSettingsFromUi() {
        CompressionSettings settings = session.settings();
        settings.setAlgorithm(algorithmCombo.getValue());
        settings.setTargetSampleRate(sampleRateSpinner.getValue());
        settings.setQuantizationLevels(quantizationSpinner.getValue());
        settings.setStepSize(stepSizeSpinner.getValue());
        settings.setInitialStepSize(initialStepSpinner.getValue());
        settings.setMinStepSize(minStepSpinner.getValue());
        settings.setMaxStepSize(maxStepSpinner.getValue());
        settings.setAdaptationFactor(adaptationSpinner.getValue());
        return settings;
    }

    private void updateSettingsVisibility(CompressionAlgorithm algorithm) {
        if (algorithm == null) {
            return;
        }
        boolean dpcm = algorithm == CompressionAlgorithm.DPCM;
        boolean dm = algorithm == CompressionAlgorithm.DELTA_MODULATION;
        boolean adm = algorithm == CompressionAlgorithm.ADAPTIVE_DELTA_MODULATION;

        setVisible(settingsQuantizationLabel, dpcm);
        setVisible(quantizationSpinner, dpcm);
        setVisible(settingsStepSizeLabel, dm);
        setVisible(stepSizeSpinner, dm);
        setVisible(settingsInitialStepLabel, adm);
        setVisible(initialStepSpinner, adm);
        setVisible(settingsMinStepLabel, adm);
        setVisible(minStepSpinner, adm);
        setVisible(settingsMaxStepLabel, adm);
        setVisible(maxStepSpinner, adm);
        setVisible(settingsAdaptationLabel, adm);
        setVisible(adaptationSpinner, adm);
    }

    private static void setVisible(javafx.scene.Node node, boolean visible) {
        node.setVisible(visible);
        node.setManaged(visible);
    }

    @FXML
    private void onTogglePlayback() {
        if (!session.canPlay()) {
            return;
        }
        if (playbackService.isPlaying()) {
            playbackService.stop();
            return;
        }
        try {
            AudioMetadata metadata = session.playbackMetadata();
            playbackService.play(session.workingSamples(), metadata);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onCompress() {
        if (!session.hasOriginalAudio()) {
            return;
        }
        playbackService.stop();
        readSettingsFromUi();
        monitor.reset();
        monitor.setOriginalSizeBytes(session.metadata().map(AudioMetadata::fileSizeBytes).orElse(0L));
        ratioSeries.getData().clear();
        speedSeries.getData().clear();
        setProcessingUi(true);
        setStatus(I18n.get("status.compressing"), StatusMode.BUSY);

        compressionService.compress(session, monitor,
                report -> Platform.runLater(() -> {
                    setProcessingUi(false);
                    refreshActionButtons();
                    setStatus(I18n.get("status.compressed"), StatusMode.IDLE);
                    reportCard.setVisible(true);
                    reportCard.setManaged(true);
                }),
                error -> Platform.runLater(() -> {
                    setProcessingUi(false);
                    if (error instanceof java.util.concurrent.CancellationException) {
                        setStatus(I18n.get("status.cancelled"), StatusMode.IDLE);
                    } else {
                        showError(error.getMessage());
                    }
                }));
    }

    @FXML
    private void onDecompress() {
        if (session.containerBytes().length == 0) {
            showError(I18n.get("report.empty"));
            return;
        }
        playbackService.stop();
        monitor.reset();
        ratioSeries.getData().clear();
        speedSeries.getData().clear();
        setProcessingUi(true);
        setStatus(I18n.get("status.decompressing"), StatusMode.BUSY);

        compressionService.decompress(session, monitor,
                () -> Platform.runLater(() -> {
                    waveformView.setSamples(session.workingSamples(),
                            session.metadata().map(AudioMetadata::channels).orElse(1));
                    setProcessingUi(false);
                    refreshActionButtons();
                    setStatus(I18n.get("status.decompressed"), StatusMode.IDLE);
                }),
                error -> Platform.runLater(() -> {
                    setProcessingUi(false);
                    if (error instanceof java.util.concurrent.CancellationException) {
                        setStatus(I18n.get("status.cancelled"), StatusMode.IDLE);
                    } else {
                        showError(error.getMessage());
                    }
                }));
    }

    @FXML
    private void onCancel() {
        compressionService.cancel();
    }

    @FXML
    private void onReset() {
        playbackService.stop();
        compressionService.cancel();
        session.resetToOriginal();
        if (session.canPlay()) {
            session.metadata().ifPresent(meta ->
                    waveformView.setSamples(session.workingSamples(), meta.channels()));
        } else {
            waveformView.clear();
        }
        monitor.reset();
        ratioSeries.getData().clear();
        speedSeries.getData().clear();
        updateReport(null);
        setProcessingUi(false);
        setStatus(I18n.get("status.reset"), StatusMode.IDLE);
    }

    @FXML
    private void onSave() {
        if (!session.hasAudio()) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle(I18n.get("dialog.saveCompressed.title"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.get("dialog.filter.audc"), "*.audc"));
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.get("dialog.filter.wav"), "*.wav"));
        File file = chooser.showSaveDialog(primaryStage);
        if (file == null) {
            return;
        }
        try {
            String name = file.getName().toLowerCase(Locale.ROOT);
            if (name.endsWith(".audc") && session.containerBytes().length > 0) {
                exportService.saveCompressed(file, session.containerBytes());
            } else {
                if (!name.endsWith(".wav")) {
                    file = new File(file.getParentFile(), file.getName() + ".wav");
                }
                AudioMetadata metadata = session.playbackMetadata();
                exportService.saveDecompressedWav(file, session.workingSamples(), metadata);
            }
            setStatus(I18n.get("status.saved", file.getName()), StatusMode.IDLE);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onExit() {
        playbackService.stop();
        compressionService.shutdown();
        loader.shutdownNow();
        Platform.exit();
    }

    private void updateMetadata(AudioMetadata metadata) {
        metaNameLabel.setText(metadata.fileName());
        metaSizeLabel.setText(metadata.formattedSize());
        metaDurationLabel.setText(metadata.formattedDuration());
        metaSampleRateLabel.setText(I18n.get("value.hz", (int) metadata.sampleRate()));
        metaChannelsLabel.setText(I18n.get("value.channels", metadata.channels()));
        metaBitRateLabel.setText(I18n.get("value.bitRate", metadata.bitRate()));
        metaEncodingLabel.setText(metadata.encodingType());
    }

    private void clearMetadataLabels() {
        String na = I18n.get("value.notAvailable");
        metaNameLabel.setText(na);
        metaSizeLabel.setText(na);
        metaDurationLabel.setText(na);
        metaSampleRateLabel.setText(na);
        metaChannelsLabel.setText(na);
        metaBitRateLabel.setText(na);
        metaEncodingLabel.setText(na);
    }

    private void updateReport(CompressionReport report) {
        if (report == null) {
            reportEmptyLabel.setVisible(true);
            reportEmptyLabel.setManaged(true);
            reportContent.setVisible(false);
            reportContent.setManaged(false);
            reportCard.setVisible(session.hasAudio());
            reportCard.setManaged(session.hasAudio());
            return;
        }
        reportEmptyLabel.setVisible(false);
        reportEmptyLabel.setManaged(false);
        reportContent.setVisible(true);
        reportContent.setManaged(true);
        reportCard.setVisible(true);
        reportCard.setManaged(true);

        reportOriginalLabel.setText(report.formattedOriginalSize());
        reportCompressedLabel.setText(report.formattedCompressedSize());
        reportSavingsLabel.setText(String.format("%.1f%%", report.savingsPercent()));
        reportElapsedLabel.setText(report.formattedElapsed());
        reportAlgorithmLabel.setText(I18n.get(report.algorithm().i18nKey()));
        reportSettingsLabel.setText(report.settings().summary());
        reportAvgSpeedLabel.setText(I18n.get("value.samplesPerSec", report.averageSamplesPerSecond()));
        reportPeakSpeedLabel.setText(I18n.get("value.samplesPerSec", report.peakSamplesPerSecond()));
    }

    private void showOptionsForAudio(boolean hasAudio) {
        optionsPlaceholder.setVisible(!hasAudio);
        optionsPlaceholder.setManaged(!hasAudio);
        settingsCard.setVisible(hasAudio);
        settingsCard.setManaged(hasAudio);
        reportCard.setVisible(hasAudio);
        reportCard.setManaged(hasAudio);
        if (!hasAudio) {
            updateReport(null);
        }
    }

    private void updateWorkspaceEmpty(boolean empty) {
        workspaceEmpty.setVisible(empty);
        workspaceEmpty.setManaged(empty);
        waveformHost.setVisible(!empty);
        waveformHost.setManaged(!empty);
        progressBar.setVisible(!empty);
        progressBar.setManaged(!empty);
        ratioChart.setVisible(!empty);
        ratioChart.setManaged(!empty);
        speedChart.setVisible(!empty);
        speedChart.setManaged(!empty);
    }

    private void setActionButtonsEnabled(boolean enabled) {
        if (!enabled) {
            btnPlay.setDisable(true);
            btnCompress.setDisable(true);
            btnDecompress.setDisable(true);
            btnSave.setDisable(true);
            btnReset.setDisable(true);
            return;
        }
        refreshActionButtons();
    }

    private void refreshActionButtons() {
        boolean loaded = session.hasAudio();
        btnPlay.setDisable(!session.canPlay());
        btnCompress.setDisable(!session.hasOriginalAudio());
        btnDecompress.setDisable(session.containerBytes().length == 0);
        btnSave.setDisable(!loaded);
        btnReset.setDisable(!loaded);
    }

    private void setProcessingUi(boolean processing) {
        btnOpen.setDisable(processing);
        if (processing) {
            btnPlay.setDisable(true);
            btnCompress.setDisable(true);
            btnDecompress.setDisable(true);
            btnSave.setDisable(true);
            btnReset.setDisable(true);
        } else {
            refreshActionButtons();
        }
        btnCancel.setVisible(processing);
        btnCancel.setManaged(processing);
        statusSpinner.setVisible(processing);
        statusSpinner.setManaged(processing);
        statusDotPane.setVisible(!processing);
        statusDotPane.setManaged(!processing);
        algorithmCombo.setDisable(processing);
        sampleRateSpinner.setDisable(processing);
        quantizationSpinner.setDisable(processing);
        stepSizeSpinner.setDisable(processing);
        initialStepSpinner.setDisable(processing);
        minStepSpinner.setDisable(processing);
        maxStepSpinner.setDisable(processing);
        adaptationSpinner.setDisable(processing);
    }

    private void updateProcessingUi(ProcessingState state) {
        boolean busy = state == ProcessingState.COMPRESSING || state == ProcessingState.DECOMPRESSING;
        setProcessingUi(busy);
    }

    private void bindLocalizedTexts() {
        appLogoLabel.setText(I18n.get("app.title"));
        btnOpen.setText(I18n.get("action.open"));
        btnPlay.setText(I18n.get("action.play"));
        btnCompress.setText(I18n.get("action.compress"));
        btnDecompress.setText(I18n.get("action.decompress"));
        btnSave.setText(I18n.get("action.save"));
        btnReset.setText(I18n.get("action.reset"));
        btnCancel.setText(I18n.get("action.cancel"));
        btnOpenEmpty.setText(I18n.get("action.open"));
        menuFile.setText(I18n.get("menu.file"));
        menuOpen.setText(I18n.get("menu.open"));
        menuSave.setText(I18n.get("menu.save"));
        menuReset.setText(I18n.get("menu.reset"));
        menuExit.setText(I18n.get("menu.exit"));
        emptyTitleLabel.setText(I18n.get("empty.title"));
        emptySubtitleLabel.setText(I18n.get("empty.subtitle"));
        sidebarTitleLabel.setText(I18n.get("sidebar.title"));
        sidebarSectionMetadata.setText(I18n.get("sidebar.metadata"));
        metaKeyName.setText(I18n.get("metadata.name"));
        metaKeySize.setText(I18n.get("metadata.size"));
        metaKeyDuration.setText(I18n.get("metadata.duration"));
        metaKeySampleRate.setText(I18n.get("metadata.sampleRate"));
        metaKeyChannels.setText(I18n.get("metadata.channels"));
        metaKeyBitRate.setText(I18n.get("metadata.bitRate"));
        metaKeyEncoding.setText(I18n.get("metadata.encoding"));
        optionsPlaceholderTitle.setText(I18n.get("options.placeholder.title"));
        optionsPlaceholderText.setText(I18n.get("options.placeholder.text"));
        sidebarSectionSettings.setText(I18n.get("sidebar.settings"));
        settingsAlgorithmLabel.setText(I18n.get("settings.algorithm"));
        settingsSampleRateLabel.setText(I18n.get("settings.sampleRate"));
        settingsQuantizationLabel.setText(I18n.get("settings.quantizationLevels"));
        settingsStepSizeLabel.setText(I18n.get("settings.stepSize"));
        settingsInitialStepLabel.setText(I18n.get("settings.initialStep"));
        settingsMinStepLabel.setText(I18n.get("settings.minStep"));
        settingsMaxStepLabel.setText(I18n.get("settings.maxStep"));
        settingsAdaptationLabel.setText(I18n.get("settings.adaptationFactor"));
        sidebarSectionReport.setText(I18n.get("sidebar.report"));
        reportEmptyLabel.setText(I18n.get("report.empty"));
        reportKeyOriginal.setText(I18n.get("report.originalSize"));
        reportKeyCompressed.setText(I18n.get("report.compressedSize"));
        reportKeySavings.setText(I18n.get("report.savings"));
        reportKeyElapsed.setText(I18n.get("report.elapsed"));
        reportKeyAlgorithm.setText(I18n.get("report.algorithm"));
        reportKeySettings.setText(I18n.get("report.settings"));
        reportKeyAvgSpeed.setText(I18n.get("report.avgSpeed"));
        reportKeyPeakSpeed.setText(I18n.get("report.peakSpeed"));
        ratioChart.setTitle(I18n.get("chart.ratio"));
        speedChart.setTitle(I18n.get("chart.speed"));
        ratioTimeAxis.setLabel(I18n.get("chart.time.axis"));
        ratioValueAxis.setLabel(I18n.get("chart.ratio.axis"));
        speedTimeAxis.setLabel(I18n.get("chart.time.axis"));
        speedValueAxis.setLabel(I18n.get("chart.speed.axis"));
        shortcutsLabel.setText(I18n.get("status.shortcuts"));
    }

    private void applyDirection(Locale locale) {
        if (locale != null && "ar".equals(locale.getLanguage())) {
            root.setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
        } else {
            root.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }
        if (sidebarScroll != null) {
            sidebarScroll.setNodeOrientation(NodeOrientation.LEFT_TO_RIGHT);
        }
    }

    private boolean isSupportedAudio(File file) {
        return SupportedAudioFormats.isSupported(file);
    }

    private void showError(String message) {
        setStatus(I18n.get("status.error", message != null ? message : ""), StatusMode.ERROR);
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(I18n.get("dialog.error.title"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private enum StatusMode { IDLE, BUSY, ERROR }

    private void setStatus(String message, StatusMode mode) {
        statusLabel.setText(message);
        statusDotPane.getStyleClass().removeAll("status-dot-idle", "status-dot-busy");
        if (mode == StatusMode.BUSY) {
            statusDotPane.getStyleClass().add("status-dot-busy");
        } else {
            statusDotPane.getStyleClass().add("status-dot-idle");
        }
    }
}
