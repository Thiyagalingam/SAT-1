package com.project.traceability.layout;

import org.gephi.graph.api.GraphController;
import org.gephi.io.generator.plugin.RandomGraph;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ContainerFactory;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.AutoLayout;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.layout.plugin.forceAtlas.ForceAtlasLayout;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import java.util.concurrent.TimeUnit;

public class LayoutUpdaterFct {

    private final PreviewJFrame previewJFrame;

    public LayoutUpdaterFct(PreviewJFrame previewJFrame)
    {
        this.previewJFrame = previewJFrame;
    }

    private AutoLayout CreateAutoLayout(final Workspace workspace)
    {
        AutoLayout autoLayout = new AutoLayout(1, TimeUnit.MILLISECONDS);
        autoLayout.setGraphModel(Lookup.getDefault().lookup(GraphController.class).getModel(workspace));
        YifanHuLayout firstLayout = new YifanHuLayout(null, new StepDisplacement(1f));
        ForceAtlasLayout secondLayout = new ForceAtlasLayout(null);
        AutoLayout.DynamicProperty adjustBySizeProperty = AutoLayout.createDynamicProperty("forceAtlas.adjustSizes.name", Boolean.TRUE, 0.1f);//True after 10% of layout time
        AutoLayout.DynamicProperty repulsionProperty = AutoLayout.createDynamicProperty("forceAtlas.repulsionStrength.name", new Double(50.), 0f);//500 for the complete period
        autoLayout.addLayout(firstLayout, 0.1f);
        autoLayout.addLayout(secondLayout, 0.9f, new AutoLayout.DynamicProperty[]{adjustBySizeProperty, repulsionProperty});
        return autoLayout;
    }

    public Runnable createLayoutRunnable() {
        return new Runnable() {

            public void run() {
                    AutoLayout autoLayout = CreateAutoLayout(previewJFrame.workspace);
                    autoLayout.execute();
                    synchronized (someObject){
                    previewJFrame.Update();
                }
            }
        };
    }

    Object someObject = new Object();

    private void GenRandGraph(Container container)
    {
        RandomGraph randomGraph = new RandomGraph();
        randomGraph.setNumberOfNodes(20);
        randomGraph.setWiringProbability(0.2);
        randomGraph.generate(container.getLoader());
    }

    private void Import(Container container,Workspace workspace)
    {
        ImportController importController = Lookup.getDefault().lookup(ImportController.class);
        importController.process(container, new DefaultProcessor(), workspace);
    }

    public Runnable fillGraphRunnable() {
        return new Runnable() {

            public void run() {

                        Container container = Lookup.getDefault().lookup(ContainerFactory.class).newContainer();
                        GenRandGraph(container);

                        synchronized (someObject){
                            Import(container, previewJFrame.workspace);
                            previewJFrame.Update();
                        }

            }
        };
    }
}
