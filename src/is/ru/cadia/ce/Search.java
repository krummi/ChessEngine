package is.ru.cadia.ce;

import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.board.Evaluation;
import is.ru.cadia.ce.other.Constants;
import is.ru.cadia.ce.other.Options;
import is.ru.cadia.ce.protocols.ProtocolHandler;
import is.ru.cadia.ce.transposition.TranspositionTable;

import java.util.ArrayList;
import java.util.LinkedList;

public class Search implements Constants {

    // Search configuration (default settings can be adjusted in Options.java)

    // Multi-Cut
    private static boolean DO_MULTI_CUT;
    private static int MC_EXPAND;
    private static int MC_REDUCTION;
    private static int MC_CUTOFFS;
    private static boolean MC_PIECE_CHECK;

    // Null Move
    private static boolean DO_NULL_MOVES;
    private static int NM_REDUCTION;

    // Late-Move Reductions
    private static boolean DO_LMR;
    private static int LMR_FULL_DEPTH_MOVES;

    // Aspiration window
    private static int ASPIRATION_SIZE;

    // Constants

    private static final int CONTEMPT_FACTOR = 15;  // TODO: fix this.
    private static final int MAX_ITERATIONS = 100;

    public static final int NODE_ALL = 0;
    public static final int NODE_CUT = 1;
    public static final int NODE_PV = 2;

    // Time management
    private static final int TIME_CHECK_INTERVAL = 1000;

    // Variables

    // Necessary data structures
    public TranspositionTable transTable;       // A instance of a transposition table.
    private MoveSelector[] selectors;           // The selectors used for each ply.
    private ProtocolHandler handler;            // The ProtocolHandler-object associated

    // Time management
    private int timeForThisMove, pollForStopInterval = TIME_CHECK_INTERVAL;
    private boolean useFixedDepth = false, abortSearch = false;
    private long timeStarted;

    // Search information
    public long nodesSearched, qsearched;

    // Functions

    public Search(ProtocolHandler handler) {
        this.handler = handler;

        // Initialize
        initialize();
    }

    private void initialize() {

        // Initializes the PlyInfo instances that will be used.
        selectors = new MoveSelector[MAX_PLY];

        for (int a = 0; a < MAX_PLY; a++) {
            selectors[a] = new MoveSelector();
        }

        // Initialize transposition table: TODO: Move me?
        transTable = new TranspositionTable();

        // Initializes the configuration
        Options options = Options.getInstance();

        DO_MULTI_CUT = options.getOptionBoolean("Do MultiCut");
        MC_CUTOFFS = options.getOptionInt("MC Cutoffs");
        MC_EXPAND = options.getOptionInt("MC Expand");
        MC_REDUCTION = options.getOptionInt("MC Reduction");
        MC_PIECE_CHECK = options.getOptionBoolean("MC Piece");

        DO_NULL_MOVES = options.getOptionBoolean("Do NullMove");
        NM_REDUCTION = options.getOptionInt("NM Reduction");

        DO_LMR = options.getOptionBoolean("Do LMR");
        LMR_FULL_DEPTH_MOVES = options.getOptionInt("LMR FullDepthMoves");

        ASPIRATION_SIZE = options.getOptionInt("Aspiration Size");
    }

    // Couldn't resist to steal the "think" name from Glaurung
    public int think(Board board, int depth) {

        // Initialization

        nodesSearched = 0;
        qsearched = 0;
        abortSearch = false;
        timeStarted = System.currentTimeMillis();
        useFixedDepth = (depth != 0);

        if (!useFixedDepth) {
            timeForThisMove = handler.getTimeForThisMove(board.sideToMove);
        }

        int iteration = 1;
        int bestMove = Move.MOVE_NONE;
        int alpha = -Value.INFINITY, beta = Value.INFINITY;

        while (iteration <= MAX_ITERATIONS) {

            // Resets the stop poll interval
            pollForStopInterval = TIME_CHECK_INTERVAL;

            // Does the search for this iteration
            int eval = searchRoot(board, iteration, alpha, beta, bestMove);

            // Checks to see if we should stop.
            if (abortSearch) break;

            // TODO: change as on page 20 in Yngvis thesis.
            // Check if aspiration window failed, and if so; search with a wider window
            if (eval <= alpha) {
                alpha = -Value.INFINITY;
                continue;
            } else if (eval >= beta) {
                beta = Value.INFINITY;
                continue;
            }

            // The aspiration window search did not fail.
            int[] pvArray = new int[MAX_PLY];
            int noOfMoves = transTable.extractPV(board, pvArray);

            // PV to string:
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < noOfMoves; c++) sb.append(Move.toLAN(pvArray[c])).append(" ");

            long timeUsed = System.currentTimeMillis() - timeStarted;
            double timeUsedPerSec = timeUsed / 1000d;
            int nps = (int) (timeUsedPerSec <= 1. ? nodesSearched : nodesSearched / timeUsedPerSec);

            // TODO: MATE BOUND: info depth 88 score mate 2 time 17 nodes 10107 nps 594529 pv h6h7 h8h7 h5g6

            String info = String.format(
                    "info score cp %d depth %d nodes %d nps %d time %d pv %s",
                    eval, iteration, nodesSearched, nps, timeUsed, sb.toString());
            handler.sendMessage(info);
            bestMove = pvArray[0];

            assert Move.isOk(bestMove) : "Invalid move: " + bestMove;

            // Adjusts the aspiration window size
            alpha = Math.max(eval - ASPIRATION_SIZE, -Value.INFINITY);
            beta = Math.min(eval + ASPIRATION_SIZE, Value.INFINITY);

            // Break if we are using fixed depth and we have searched to that specific depth
            if (useFixedDepth && iteration == depth) break;

            iteration++;
        }

        System.out.printf("Phase: %d/%d\n", board.info.gamePhase, Value.INITIAL_PHASE);
        assert bestMove != Move.MOVE_NONE;
        return bestMove;
    }

    public int searchRoot(Board board, int depth, int alpha, int beta, int pvMove) {

        // Initialization

        int eval = 0;
        int bestEval = -Value.INFINITY;
        int nodeType = NODE_ALL;
        int bestMove = Move.MOVE_NONE;

        MoveSelector selector = selectors[0];
        selector.initialize(board, pvMove, board.isCheck(), false);

        int move;
        while ((move = selector.getNextMove()) != Move.MOVE_NONE) {

            board.make(move);

            if (nodeType == NODE_PV) { // Does a Principal Variation Search (PVS)
                eval = -alphaBeta(board, depth - 1, 1, -alpha - 1, -alpha, true);

                if (eval > alpha && eval < beta) {
                    eval = -alphaBeta(board, depth - 1, 1, -beta, -alpha, true);
                }
            } else { // Normal search
                eval = -alphaBeta(board, depth - 1, 1, -beta, -alpha, true);
            }

            board.retract(move);

            if (abortSearch) break;

            if (eval > bestEval) {
                if (eval >= beta) {
                    // Beta cutoff!
                    if (!abortSearch) {
                        transTable.put(board.key, NODE_CUT, depth, eval, move);
                    }

                    return beta;
                }

                bestEval = eval;
                bestMove = move;

                if (eval > alpha) {
                    nodeType = NODE_PV;
                    alpha = eval;
                }
            }
        }

        // If any move was found, put it into the transposition table
        if (bestMove != Move.MOVE_NONE) {
            transTable.put(board.key, nodeType, depth, eval, bestMove);
        }

        return alpha;
    }

    // DEBUG
    public int mcprunes = 0;
    public long transFound = 0, transLessDepth = 0, transNotFound = 0, transExact = 0, transAlpha = 0, transBeta = 0;
    public long before = 0, after = 0;

    public int alphaBeta(Board board, int depth, int ply, int alpha, int beta, boolean nmAllowed) {

        // Time management

        if (!useFixedDepth && --pollForStopInterval == 0) {
            pollForStopInterval = TIME_CHECK_INTERVAL;
            if (System.currentTimeMillis() > (timeStarted + timeForThisMove)) {
                abortSearch = true;
                return 0;
            }
        }

        // Increment the node count

        nodesSearched++;

        // Repetition detection

        if (board.isDraw()) return Value.DRAW;

        // Transposition table lookup

        boolean mcAllowed = false;

        TranspositionTable.HashEntry entry = transTable.get(board.key);
        if (entry != null) {
            transFound++;
            if (entry.depth >= depth) {
                if (entry.type == NODE_PV) {
                    return entry.eval;
                } else if (entry.type == NODE_ALL && entry.eval <= alpha) {
                    return entry.eval; // TODO: change to return alpha?
                } else if (entry.type == NODE_CUT && entry.eval >= beta) {
                    return entry.eval; // TODO: change to return beta?
                }
            } else {
                if (entry.type == NODE_CUT && entry.eval >= beta) {
                    mcAllowed = true;
                }
            }
        } else {
            transNotFound++;
        }

        // Horizon?

        int eval;
        if (depth <= 0) {
            eval = qsearch(board, ply, alpha, beta);
            transTable.putLeaf(board.key, eval, alpha, beta);
            return eval;
        }

        // Move generation

        int move;
        boolean isCheck = board.isCheck();
        MoveSelector selector = selectors[ply];
        selector.initialize(board, (entry == null ? Move.MOVE_NONE : entry.move), isCheck, false);

        // Checks for mate or stalemate

        if ((move = selector.getNextMove()) == Move.MOVE_NONE) {
            return isCheck ? (-Value.MATE + ply) : Value.DRAW;
        }

        // Null move pruning

        if (DO_NULL_MOVES
                && nmAllowed
                && depth >= 2
                && !isCheck
                && board.info.material[board.sideToMove] > board.info.pawnMaterial[board.sideToMove]) {

            int epSquare = board.epSquare;

            board.makeNullMove();
            eval = -alphaBeta(board, depth - NM_REDUCTION, ply + 1, -beta, -beta + 1, false);
            board.retractNullMove(epSquare);

            if (eval >= beta) {
                if (!abortSearch) {
                    transTable.put(board.key, NODE_CUT, depth, eval, move);
                }
                return eval;
            }
        }

        if (abortSearch) {
            return 0;
        }

        int bestEval = -Value.INFINITY;
        int bestMove = Move.MOVE_NONE;
        int nodeType = NODE_ALL;
        LinkedList<Integer> moves = null;

        //int[] moveQueue = new int[MC_EXPAND];
        //moveQueue[0] = move;
        //int queueIndex = 0, queueSize = 0;

        before++;

        if (DO_MULTI_CUT
                && depth >= MC_REDUCTION
                && !isCheck
                && mcAllowed) {

            after++;

            ArrayList<Integer> cuts = new ArrayList<Integer>(MC_CUTOFFS);
            moves = new LinkedList<Integer>();
            int c = 0;

            while (true) {

                int from = Move.getFrom(move);
                boolean causedCutoff = false;

                // Distinguish between MC_PIECE_CHECK on and off;
                if ((MC_PIECE_CHECK && !cuts.contains(from)) || !MC_PIECE_CHECK) {

                    board.make(move);
                    eval = -alphaBeta(board, depth - 1 - MC_REDUCTION, ply + 1, -beta, -alpha, true);
                    board.retract(move);

                    if (eval >= beta) {

                        causedCutoff = true;
                        if (MC_PIECE_CHECK) cuts.add(from);

                        if (++c == MC_CUTOFFS) {
                            mcprunes++;
                            //if (!abortSearch) {
                            //    transTable.put(board.key, NODE_CUT, depth, eval, Move.MOVE_NONE);
                            //}
                            return beta;
                        }
                    }

                }

                if (causedCutoff) {
                    moves.add(c - 1, move);
                } else {
                    moves.addLast(move);
                }

                if (moves.size() == MC_EXPAND) break;

                move = selector.getNextMove();
                if (move == Move.MOVE_NONE) break;
                //moveQueue[queueIndex] = move;
            }

            //queueSize = queueIndex;
            //queueIndex = 0;
            //move = moveQueue[queueIndex++];
            move = moves.removeFirst();
        }

        int movesSearched = 0;

        while (true) {

            board.make(move);

            if (nodeType == NODE_PV) {

                // Late Move Reduction

                int type = Move.getType(move);
                if (DO_LMR
                        && movesSearched >= LMR_FULL_DEPTH_MOVES
                        && depth >= 3
                        && !isCheck
                        && !Move.isCapture(type)
                        && !Move.isPromotion(type)) {
                    eval = -alphaBeta(board, depth - 2, ply + 1, -alpha - 1, -alpha, true);
                } else {
                    eval = alpha + 1;
                }

                if (eval > alpha) {
                    // Does a Principal Variation Search (PVS)
                    eval = -alphaBeta(board, depth - 1, ply + 1, -alpha - 1, -alpha, true);

                    if (eval > alpha && eval < beta) {
                        eval = -alphaBeta(board, depth - 1, ply + 1, -beta, -alpha, true);
                    }
                }
            } else {
                eval = -alphaBeta(board, depth - 1, ply + 1, -beta, -alpha, true);
            }

            board.retract(move);

            movesSearched++;

            if (eval > bestEval) {
                if (eval >= beta) {

                    // Beta cutoff
                    if (!abortSearch) {
                        transTable.put(board.key, NODE_CUT, depth, eval, move);
                    }
                    return beta;
                }

                bestEval = eval;
                bestMove = move;

                if (eval > alpha) {
                    nodeType = NODE_PV;
                    alpha = eval;
                }
            }

            if (!abortSearch) {
                transTable.put(board.key, nodeType, depth, bestEval, bestMove);
            }

            // Get then next move from the queue or from the move stack, if the queue is empty:
            /*if (queueSize <= queueIndex) {
                move = selector.getNextMove();
                if (move == Move.MOVE_NONE) break;
            } else {
                move = moveQueue[queueIndex++];
            }*/
            if (moves != null && !moves.isEmpty()) {
                move = moves.removeFirst();
            } else {
                move = selector.getNextMove();
                if (move == Move.MOVE_NONE) break;
            }
        }

        return alpha;
    }

    public int qsearch(Board board, int ply, int alpha, int beta) {

        nodesSearched++;
        qsearched++;

        int eval = Evaluation.evaluate(board, false);

        if (eval >= beta) {
            return beta;
        }
        if (eval > alpha) {
            alpha = eval;
        }

        boolean isCheck = board.isCheck();
        MoveSelector selector = selectors[ply];
        selector.initialize(board, Move.MOVE_NONE, isCheck, true);

        int move;
        while ((move = selector.getNextMove()) != Move.MOVE_NONE) {

            board.make(move);
            eval = -qsearch(board, ply + 1, -beta, -alpha);
            board.retract(move);

            if (eval >= beta) {
                return beta;
            }
            if (eval > alpha) {
                alpha = eval;
            }
        }

        return alpha;
    }

    public String getConfiguration() {
        return String.format("MC=%s (c: %d, e: %d, r: %d, piece: %s), NM=%s (r: %d), LMR=%s (fdm: %d), AspSize: %d",
                (DO_MULTI_CUT ? "on" : "off"),
                MC_CUTOFFS,
                MC_EXPAND,
                MC_REDUCTION,
                (MC_PIECE_CHECK ? "on" : "off"),
                (DO_NULL_MOVES ? "on" : "off"),
                NM_REDUCTION,
                (DO_LMR ? "on" : "off"),
                LMR_FULL_DEPTH_MOVES,
                ASPIRATION_SIZE
        );
    }

}