package jmri.jmrit.beantable;

import java.awt.GraphicsEnvironment;

import javax.swing.JFrame;

import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.AbstractProxyManager;
import jmri.managers.DefaultIdTagManager;
import jmri.managers.ProxyIdTagManager;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.netbeans.jemmy.operators.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class IdTagTableTabActionTest extends AbstractTableTabActionBase {

    @Test
    public void testCTor() {
        Assert.assertNotNull("exists", a);
    }

    @Override
    public String getTableFrameName() {
        return Bundle.getMessage("TitleIdTagTable");
    }

    /**
     * Check the return value of includeAddButton.
     * <p>
     * The table generated by this action includes an Add Button.
     */
    @Override
    @Test
    public void testIncludeAddButton() {
        Assert.assertTrue("Default include add button", a.includeAddButton());
    }

    @Test
    @Override
    @Disabled("parent class test causes an NPE; need to investigate cause")
    public void testGetPanel() {
    }
    
    @Disabled("v4.23.4ish Proxy returning 3 IdTag Managers, not the 2 created")
    @Test
    public void testMultiSystemTabs(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitUtil.resetInstanceManager();
        // Not returning null v4.23.4ish
        // Assert.assertNull("Null Manager at start",InstanceManager.getNullableDefault(IdTagManager.class));
    
        ProxyIdTagManager l = new ProxyIdTagManager(); // has 2 systems: ID, JD
        l.addManager(
            new DefaultIdTagManager(new InternalSystemConnectionMemo("J", "Juliet")) {
                @Override
                public void init() {} // init override to prevent loading xml data and registering shutdown task.
            });
        l.addManager(
            new DefaultIdTagManager(new InternalSystemConnectionMemo("I", "India")) {
                @Override
                public void init() {} // init override to prevent loading xml data and registering shutdown task.
            });
        InstanceManager.setIdTagManager(l);
        
        // Test that Proxy IdTag Manager has Juliet, India, and nothing else.
        IdTagManager plm = InstanceManager.getDefault(IdTagManager.class);
        if (!(plm instanceof AbstractProxyManager)) {
            Assert.fail("Instance IdTagManager Not a proxy IdTag Manager");
        }
        
        try {
            @SuppressWarnings("unchecked")
            AbstractProxyManager<IdTag> proxy = (AbstractProxyManager<IdTag>) plm;
            int numLm = proxy.getDisplayOrderManagerList().size();
            Assert.assertEquals("2 IdTag Managers, Juliet, India",2, numLm);
            
            String s = proxy.getDisplayOrderManagerList().get(0).getMemo().getUserName();
            Assert.assertEquals("IdTag Manager 0 , Juliet","Juliet", s);
            
            s = proxy.getDisplayOrderManagerList().get(1).getMemo().getUserName();
            Assert.assertEquals("IdTag Manager 1, India","India", s);
        } catch (ClassCastException e){
            Assert.fail("catch Instance IdTagManager Not a proxy IdTag Manager");
        }
        
        
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("ID1");
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("ID2");
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("ID3");
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("ID4");
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("ID5");
        
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("JD8");
        InstanceManager.getDefault(IdTagManager.class).provideIdTag("JD9");
        
        TabbedIdTagTableFrame sf = new TabbedIdTagTableFrame();
        sf.initTables();
        sf.initComponents();
        sf.pack();
        sf.setVisible(true);
        
        JFrame f = JFrameOperator.waitJFrame(sf.getTitle(), true, true);
        JFrameOperator jfo = new JFrameOperator(f);
        JTabbedPaneOperator tabOperator = new JTabbedPaneOperator(jfo);
        Assert.assertEquals("3 manager tabs",3, tabOperator.getTabCount());
        
        tabOperator.selectPage("All");
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        JTableOperator controltbl = new JTableOperator(jfo, 0);
        Assert.assertEquals("All tab 7 IdTag",7, controltbl.getRowCount());
        
        tabOperator.selectPage("Juliet");
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        controltbl = new JTableOperator(jfo, 0);
        Assert.assertEquals("Juliet tab 2 IdTag",2, controltbl.getRowCount());
        
        tabOperator.selectPage("India");
        new org.netbeans.jemmy.QueueTool().waitEmpty();
        controltbl = new JTableOperator(jfo, 0);
        Assert.assertEquals("India tab 5 IdTag",5, controltbl.getRowCount());
        
        // jmri.util.swing.JemmyUtil.pressButton(jfo, "Not a Button, pause test");
        jfo.requestClose();
    
    }
    
    private static class TabbedIdTagTableFrame extends ListedTableFrame<IdTag> {
        
        public TabbedIdTagTableFrame(){
            super();
            tabbedTableItemListArrayArray.clear(); // reset static BeanTable list
        }
        
        @Override
        public void initTables() {
            addTable("jmri.jmrit.beantable.IdTagTableTabAction", Bundle.getMessage("MenuItemIdTagTable"), false);
        }
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        helpTarget = "package.jmri.jmrit.beantable.IdTagTable"; 
        a = new IdTagTableTabAction();
    }

    @AfterEach
    @Override
    public void tearDown() {
        a = null;
        JUnitUtil.tearDown();
    }

}
