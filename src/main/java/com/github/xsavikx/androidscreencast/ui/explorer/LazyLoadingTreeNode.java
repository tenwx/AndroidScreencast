package com.github.xsavikx.androidscreencast.ui.explorer;

import javax.swing.*;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public abstract class LazyLoadingTreeNode extends DefaultMutableTreeNode implements TreeWillExpandListener {

    /**
     *
     */
    private static final long serialVersionUID = -4981073521761764327L;
    private static final String ESCAPE_ACTION_NAME = "escape";
    private static final KeyStroke ESCAPE_KEY = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    /**
     * The JTree containing this Node
     */
    private JTree tree;
    /**
     * Can the worker be Canceled ?
     */
    private boolean cancelable;

    /**
     * Default Constructor
     *
     * @param userObject an Object provided by the user that constitutes the node's data
     * @param tree       the JTree containing this Node
     * @param cancelable
     */
    public LazyLoadingTreeNode(Object userObject, JTree tree, boolean cancelable) {
        super(userObject);
        tree.addTreeWillExpandListener(this);
        this.tree = tree;
        this.cancelable = cancelable;
        setAllowsChildren(true);
    }

    /**
     * @return <code>true</code> if there are some childrens
     */
    private boolean areChildrenLoaded() {
        return getChildCount() > 0 && getAllowsChildren();
    }

    /**
     * @return a new Loading please wait node
     */
    private MutableTreeNode createLoadingNode() {
        return new DefaultMutableTreeNode("Loading Please Wait ...", false);
    }

    /**
     * Create worker that will load the nodes
     *
     * @param tree the tree
     * @return the newly created SwingWorker
     */
    private com.github.xsavikx.androidscreencast.ui.worker.SwingWorker<MutableTreeNode[], ?> createSwingWorker(final JTree tree) {

        com.github.xsavikx.androidscreencast.ui.worker.SwingWorker<MutableTreeNode[], ?> worker = new com.github.xsavikx.androidscreencast.ui.worker.SwingWorker<MutableTreeNode[], Object>() {

            @Override
            protected MutableTreeNode[] doInBackground() {
                return loadChildren(tree);
            }

            @Override
            protected void done() {
                try {
                    if (!isCancelled()) {
                        MutableTreeNode[] nodes = get();
                        setAllowsChildren(nodes.length > 0);
                        setChildren(nodes);
                        unRegisterSwingWorkerForCancel(tree, this);
                    } else {
                        reset();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }

            }

        };
        registerSwingWorkerForCancel(tree, worker);
        return worker;
    }

    /**
     * If the
     *
     * @return false, this node can't be a leaf
     * @see #getAllowsChildren()
     */
    @Override
    public boolean isLeaf() {
        return !getAllowsChildren();
    }

    /**
     * This method will be executed in a background thread. If you have to do some GUI stuff use {@link SwingUtilities#invokeLater(Runnable)}
     *
     * @param tree the tree
     * @return the Created nodes
     */
    abstract MutableTreeNode[] loadChildren(JTree tree);

    /**
     * If the node is cancelable an escape Action is registered in the tree's InputMap and ActionMap that will cancel the execution
     *
     * @param tree   the tree
     * @param worker the worker to cancel
     */
    private void registerSwingWorkerForCancel(JTree tree, com.github.xsavikx.androidscreencast.ui.worker.SwingWorker<MutableTreeNode[], ?> worker) {
        if (!cancelable) {
            return;
        }
        tree.getInputMap().put(ESCAPE_KEY, ESCAPE_ACTION_NAME);
        Action action = tree.getActionMap().get(ESCAPE_ACTION_NAME);
        if (action == null) {
            CancelWorkersAction cancelWorkerAction = new CancelWorkersAction();
            cancelWorkerAction.addSwingWorker(worker);
            tree.getActionMap().put(ESCAPE_ACTION_NAME, cancelWorkerAction);
        } else {
            if (action instanceof CancelWorkersAction) {
                CancelWorkersAction cancelAction = (CancelWorkersAction) action;
                cancelAction.addSwingWorker(worker);
            }
        }
    }

    /**
     * Need some improvement ... This method should restore the Node initial state if the worker if canceled
     */
    private void reset() {
        DefaultTreeModel defaultModel = (DefaultTreeModel) tree.getModel();
        int childCount = getChildCount();
        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                defaultModel.removeNodeFromParent((MutableTreeNode) getChildAt(0));
            }
        }
        setAllowsChildren(true);
    }

    /**
     * Define nodes children
     *
     * @param nodes new nodes
     */
    private void setChildren(MutableTreeNode... nodes) {
        TreeModel model = tree.getModel();
        if (model instanceof DefaultTreeModel) {
            DefaultTreeModel defaultModel = (DefaultTreeModel) model;
            int childCount = getChildCount();
            if (childCount > 0) {
                for (int i = 0; i < childCount; i++) {
                    defaultModel.removeNodeFromParent((MutableTreeNode) getChildAt(0));
                }
            }
            for (int i = 0; i < nodes.length; i++) {
                defaultModel.insertNodeInto(nodes[i], this, i);
            }
        }
    }

    /**
     * set the loading state
     */
    private void setLoading() {
        setChildren(createLoadingNode());
        TreeModel model = tree.getModel();
        if (model instanceof DefaultTreeModel) {
            DefaultTreeModel defaultModel = (DefaultTreeModel) model;
            int[] indices = new int[getChildCount()];
            for (int i = 0; i < indices.length; i++) {
                indices[i] = i;
            }
            defaultModel.nodesWereInserted(LazyLoadingTreeNode.this, indices);
        }
    }

    /**
     * Default empty implementation, do nothing on collapse event.
     */
    @Override
    public void treeWillCollapse(TreeExpansionEvent event) {
        // ignore
    }

    /**
     * Node will expand, it's time to retrieve nodes
     */
    @Override
    public void treeWillExpand(TreeExpansionEvent event) {
        if (this.equals(event.getPath().getLastPathComponent())) {
            if (areChildrenLoaded()) {
                return;
            }
            setLoading();
            com.github.xsavikx.androidscreencast.ui.worker.SwingWorker<MutableTreeNode[], ?> worker = createSwingWorker(tree);
            worker.execute();
        }
    }

    /**
     * Remove the swingWorker from the cancellable task of the tree
     *
     * @param tree
     * @param worker
     */
    private void unRegisterSwingWorkerForCancel(JTree tree, com.github.xsavikx.androidscreencast.ui.worker.SwingWorker<MutableTreeNode[], ?> worker) {
        if (!cancelable) {
            return;
        }
        Action action = tree.getActionMap().get(ESCAPE_ACTION_NAME);
        if (action != null && action instanceof CancelWorkersAction) {
            CancelWorkersAction cancelWorkerAction = new CancelWorkersAction();
            cancelWorkerAction.removeSwingWorker(worker);
        }
    }

    /**
     * ActionMap can only store one Action for the same key, This Action Stores the list of SwingWorker to be canceled if the escape key is pressed.
     *
     * @author Thierry LEFORT 3 mars 08
     */
    protected static class CancelWorkersAction extends AbstractAction {
        /**
         *
         */
        private static final long serialVersionUID = 3173288834368915117L;
        /**
         * the SwingWorkers
         */
        private List<com.github.xsavikx.androidscreencast.ui.worker.SwingWorker<MutableTreeNode[], ?>> workers = new ArrayList<>();

        /**
         * Default constructor
         */
        private CancelWorkersAction() {
            super(ESCAPE_ACTION_NAME);
        }

        /**
         * Do the Cancel
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            for (com.github.xsavikx.androidscreencast.ui.worker.SwingWorker<MutableTreeNode[], ?> worker : workers) {
                worker.cancel(true);
            }

        }

        /**
         * Add a Cancelable SwingWorker
         */
        void addSwingWorker(com.github.xsavikx.androidscreencast.ui.worker.SwingWorker<MutableTreeNode[], ?> worker) {
            workers.add(worker);
        }

        /**
         * Remove a SwingWorker
         */
        void removeSwingWorker(com.github.xsavikx.androidscreencast.ui.worker.SwingWorker<MutableTreeNode[], ?> worker) {
            workers.remove(worker);
        }

    }
}
