(
// For development only
// ---------------------------------------------------------
Quarks.install("~/Desktop/Iannis".standardizePath);
Quarks.uninstall("Iannis");
// ---------------------------------------------------------
)

(
  s.waitForBoot({
    SynthDef.synthDefDir = "~/Desktop/Iannis/synths".standardizePath;

    s.loadDirectory(SynthDef.synthDefDir);

    s.sync;

    SynthDescLib(\iannis_synth);
    // init synthdescs for iannis' synthdefs
    SynthDescLib.getLib(\iannis_synth).read(SynthDef.synthDefDir+/+"granulator.scsyndef");

    // load iannis synthdesc lib into global lib
    SynthDescLib.getLib(\iannis_synth).synthDescs.do({|item| SynthDescLib.global.add(item)});
  });
)

TempoClock.default.tempo = 107/60;
(
b = Buffer.read(s, "~/Music/SuperCollider/livebox/Samples/02_Elemental/013SineAmplitude.wav".standardizePath);
c = Buffer.read(s, "~/Documents/Sounds/Drum loops/drumloop004BPM110(44,1kHz).wav".standardizePath);
d = Buffer.read(s, "~/Documents/Sounds/Drum loops/drumloop001BPM137(48kHz).aif".standardizePath);

e = Buffer.alloc(Server.default, 8192, 1);
f = Signal.rectWindow(8192);
e.loadCollection(f);
)

IannisProbabilisticSequencerView(\sequencer, \granulator, patternLength: 4).front;

// Synth view controller

g = Group.new;
v = IannisSynthViewController(g, \granulator);
v.parse();
v.front();

// Tabbed view

l = StaticText();
l.string = "Hello";

v = CompositeView();
v.fixedHeight = 300;
v.fixedWidth = 200;

v.layout = HLayout(l);

t = IannisTabbedView("tab", v);

t.front();

// adding a tab
m = StaticText();
m.string = "another tab";

z = CompositeView();
z.fixedHeight = 300;
z.fixedWidth = 200;

z.layout = HLayout(m);

t.addPage("tab 2", z);

Server.freeAll;
