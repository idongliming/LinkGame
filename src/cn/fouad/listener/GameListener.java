package cn.fouad.listener;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.event.MouseInputAdapter;

import cn.fouad.UI.GamePanel;
import cn.fouad.commons.LinkInfo;
import cn.fouad.commons.Piece;
import cn.fouad.service.impl.GameServiceImpl;
import cn.fouad.utils.ImageUtils;

/**
 * 对游戏的事件监听
 * 按下鼠标和释放鼠标两个操作
 */
public class GameListener extends MouseInputAdapter {
    private GameServiceImpl gameService;
    private List<Piece> selects;
    private GamePanel gamePanel;

    private JLabel gradeLabel;
    private BeginListener beginListener;

    /**
     * 游戏事件监听，完成对游戏中业务逻辑的调用
     * @param gameService 游戏逻辑
     * @param gamePanel 游戏面板
     * @param gradeLabel 游戏标签 时间等
     * @param beginListener 游戏初始化监听
     */
    public GameListener(GameServiceImpl gameService, GamePanel gamePanel,
                        JLabel gradeLabel, BeginListener beginListener) {
        this.gameService = gameService;
        this.gamePanel = gamePanel;
        this.selects = new ArrayList<>();
        this.gradeLabel = gradeLabel;
        this.beginListener = beginListener;
    }

    /**
     *
     * @param event 按下鼠标时触发
     */
    @Override
    public void mousePressed(MouseEvent event) {
        // 如果游戏面板出现了游戏结束的图片则返回
        if (gamePanel.getOverImage() != null) {
            return;
        }
        Piece[][] pieces = gameService.getPieces();
        int mouseX = event.getX();
        int mouseY = event.getY();
        Piece currentPiece = gameService.findPiece(mouseX, mouseY);
        if (currentPiece == null || currentPiece.getImage() == null)
            return;
        gamePanel.setSelectPiece(currentPiece);
        if (this.selects.size() == 0) {
            this.selects.add(currentPiece);
            gamePanel.repaint();
            return;
        }
        if (this.selects.size() == 1) {
            Piece prePiece = this.selects.get(0);
            LinkInfo linkInfo = this.gameService.link(prePiece, currentPiece);
            if (linkInfo == null) {
                this.selects.remove(0);
                this.selects.add(currentPiece);
            } else {
                gamePanel.setLinkInfo(linkInfo);
                gamePanel.setSelectPiece(null);
                long grade = gameService.countGrade();
                gradeLabel.setText(String.valueOf(grade));
                pieces[prePiece.getXIndex()][prePiece.getYIndex()] = null;
                pieces[currentPiece.getXIndex()][currentPiece.getYIndex()] = null;
                this.selects.remove(0);
                if (gameService.empty()) {
                    try {
                        gamePanel.setOverImage(ImageUtils.getImage("images/win.gif"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    this.beginListener.getTimer().cancel();
                }
            }
        }

        gamePanel.repaint();
    }

    /**
     * 当释放鼠标是重新绘图，更新游戏界面
     *
     * @param event 鼠标事件
     */
    public void mouseReleased(MouseEvent event) {
        gamePanel.repaint();
    }
}
