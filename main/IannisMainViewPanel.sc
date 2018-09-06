IannisMainViewPanel : CompositeView {
    var buttonWidth;

    *new {
        ^super.new.init();
    }

    init {
        buttonWidth = 120;

        this.fixedWidth = 200;
        this.minHeight = 300;
        this.background = Color.gray(gray: 0.3, alpha: 1);

        this.layout = VLayout(
            [this.initFilePanel(), align: \center],
            nil,
            [this.initInfoPanel(), align: \center]
        );

        this.layout.spacing = 0;
    }

    initFilePanel {
        var view = CompositeView();
        view.fixedHeight = 80;

        view.layout = VLayout(
            this.initOpenButton(),
            this.initSaveButton()
        );

        ^view;
    }

    initOpenButton {
        var button = Button();
        button.fixedWidth = buttonWidth;
        button.states = [["Open Project"]];

        ^button;
    }

    initSaveButton {
        var button = Button();
        button.fixedWidth = buttonWidth;
        button.states = [["Save Project"]];

        ^button;
    }

    initInfoPanel {
        var view = CompositeView();
        view.fixedHeight = 150;
        view.fixedWidth = this.bounds.width-20;

        view.layout = VLayout(
            this.initTempoBox(),
            this.initPeakCPUText(),
            this.initSynthNumText()
        );

        ^view;
    }

    initTempoBox {
        var view = CompositeView();
        var label = StaticText();
        var numberBox = NumberBox();
        label.align = \left;
        label.stringColor = Color.white;
        label.string = "BPM:";
        numberBox.fixedWidth = 70;

        view.layout = HLayout(
            label, nil, numberBox
        );

        ^view;
    }

    initPeakCPUText {
        var view = CompositeView();
        var label = StaticText();
        var textView = StaticText();
        label.string = "Peak CPU:";
        label.stringColor = Color.white;
        textView.stringColor = Color.white;
        label.align = \left;
        textView.align = \right;

        AppClock.sched(0.0, {
            textView.string = Server.default.peakCPU.round(0.01).asString++"%";
            1;
        });

        view.layout = HLayout(
            label, nil, textView
        );

        ^view;
    }

    initSynthNumText {
        var view = CompositeView();
        var label = StaticText();
        var textView = StaticText();
        label.string = "Num. of Voices:";
        label.stringColor = Color.white;
        textView.stringColor = Color.white;
        label.align = \left;
        textView.align = \right;

        AppClock.sched(0.0, {
            textView.string = Server.default.numSynths.asString;
            1;
        });

        view.layout = HLayout(
            label, nil, textView
        );

        ^view;
    }

}
