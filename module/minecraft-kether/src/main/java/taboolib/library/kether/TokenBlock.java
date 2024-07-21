package taboolib.library.kether;

/**
 * TabooLib
 * taboolib.library.kether.TokenBlock
 *
 * @author 坏黑
 * @since 2022/9/3 16:46
 */
public class TokenBlock {

    private final String token;
    private final boolean isBlock;

    public TokenBlock(String token, boolean isBlock) {
        this.token = token;
        this.isBlock = isBlock;
    }

    public String getToken() {
        return token;
    }

    public boolean isBlock() {
        return isBlock;
    }
}
