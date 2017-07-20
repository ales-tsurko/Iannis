(
// ---------------------------------------------------------
Quarks.install("~/Desktop/Iannis".standardizePath);
Quarks.uninstall("Iannis");
// ---------------------------------------------------------
)

(
  s.waitForBoot({
    var pathToInnerSynthDefs = "~/Desktop/Iannis/inner_synthdefs".standardizePath;
    SynthDef.synthDefDir = "~/Desktop/Iannis/synthdefs".standardizePath;

    s.loadDirectory(SynthDef.synthDefDir);

    // load inner synthdefs
    s.loadDirectory(pathToInnerSynthDefs);

    s.sync;

    SynthDescLib(\iannis_synth);
    // init synthdescs for iannis' synthdefs
    PathName(SynthDef.synthDefDir).files.do({arg pathName;
      if (pathName.extension == "scsyndef") {
        SynthDescLib
        .getLib(\iannis_synth)
        .read(pathName.fullPath);
      };
    });

    // load iannis synthdesc lib into global lib
    SynthDescLib.getLib(\iannis_synth).synthDescs.do({|item| SynthDescLib.global.add(item)});

    // survive CmdPeriod
    TempoClock.default.permanent = true;
  });
)



// Sequencer
TempoClock.default.tempo = 107/60;

IannisProbabilisticSequencerView(\sequencer, \granulator, patternLength: 4).front;

// Synth view controller
(
t = IannisSynthViewController('granulator').front;
v = IannisSynthViewController('by-alestsurko-iannis-parameq').front;
c = IannisSynthViewController('by-alestsurko-iannis-compander').front;
r = IannisSynthViewController('by-alestsurko-iannis-freeverb').front;
)
r.node.moveAfter(t.node);

// Track
t = IannisMixerTrackViewController().front();




g.do({arg synth;
  synth.release;
});

a = OSCFunc({arg msg;
  msg[0].postln;
}, '/g_queryTree.reply', g.server.addr).oneShot;
a.clear();
OSCFunc().freeAll
g.server.sendMsg("/g_queryTree", g.nodeID);
a.free

p = ProxySpace(Server.default);

p.put(0, {(2+2).postln});
p.put(1, {SinOsc.ar*0.3!2});
p[1].stop

p.clear;
p.pop;

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
m.string = "another tab 2";

z = CompositeView();
z.fixedHeight = 300;
z.fixedWidth = 200;

z.layout = HLayout(m);

t.addPage("tab 2", z);




