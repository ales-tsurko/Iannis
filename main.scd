(
    // ---------------------------------------------------------
    Quarks.install("~/Desktop/Iannis".standardizePath);
    Quarks.uninstall("Iannis");
    // ---------------------------------------------------------
)
s.quit();
(
    Server.local.options.blockSize = 64;
    Server.local.options.memSize = 2 ** 20;

    s.options.numAudioBusChannels = 2048;
    s.options.numControlBusChannels = 2048;

    ~initSynthDescs = {arg dir, libName;
        var path = PathName(dir);
        path.filesDo({arg file;
            if (file.extension == "scsyndef") {
                SynthDescLib.getLib(libName).read(file);
            }
        });
    };

    s.waitForBoot({
        SynthDef.synthDefDir = "~/Desktop/Iannis/synthdefs".standardizePath;

        s.loadDirectory(SynthDef.synthDefDir);

        s.loadDirectory("~/Desktop/Iannis/inner_synthdefs".standardizePath);

        s.sync;

        SynthDescLib(\iannis_synth);
        // init synthdescs for iannis' synthdefs
        ~initSynthDescs.value(SynthDef.synthDefDir, \iannis_synth);

        // load iannis synthdesc lib into global lib
        SynthDescLib.getLib(\iannis_synth).synthDescs.do({|item| SynthDescLib.global.add(item)});
    });
)


// Sequencer
// TempoClock.default.tempo = 107/60;

// IannisProbabilisticSequencerView(\sequencer, \granulator, patternLength: 4).front;

// Synth view controller
(
    // g = IannisNodeGroup();
    // v = IannisSynthViewController(g, \granulator).front();
)

// mixer track
// t = IannisMixerTrackViewController().front();

// mixer
m = IannisMixerViewController().front();

s.queryAllNodes;
s.meter;
s.options.numAudioBusChannels;


// Tabbed view

// l = StaticText();
// l.string = "Hello";
// 
// v = CompositeView();
// v.fixedHeight = 300;
// v.fixedWidth = 200;
// 
// v.layout = HLayout(l);
// 
// t = IannisTabbedView("tab", v);
// 
// t.front();

// adding a tab
// m = StaticText();
// m.string = "another tab 2";
// 
// z = CompositeView();
// z.fixedHeight = 300;
// z.fixedWidth = 200;
// 
// z.layout = HLayout(m);
// 
// t.addPage("tab 2", z);
