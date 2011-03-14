package is.ru.cadia.ce;

import is.ru.cadia.ce.board.Board;
import is.ru.cadia.ce.board.Evaluation;
import is.ru.cadia.ce.board.MoveGenerator;
import is.ru.cadia.ce.other.Constants;
import is.ru.cadia.ce.protocols.ProtocolHandler;
import is.ru.cadia.ce.transposition.TranspositionTable;

public class Search implements Constants {

    // Constants

    private static final boolean DO_MULTI_CUT = true;
    private static final int MC_EXPAND = 10;
    private static final int MC_REDUCTION = 2;
    private static final int MC_CUTOFFS = 3;

    private static final boolean DO_NULL_MOVES = true;
    private static final int NULL_MOVE_REDUCTION = 2;

    private static final boolean DO_LMR = true;
    private static final int LMR_FULL_DEPTH_MOVES = 4;

    public static final int HASH_ALPHA = 0;
    public static final int HASH_BETA = 1;
    public static final int HASH_EXACT = 2;

    private static final int ASPIRATION_SIZE = 80;
    private static final int TIME_CHECK_INTERVAL = 500;

    // Variables

    public TranspositionTable transTable;   // A instance of a transposition table.

    private MoveSelector[] selectors;       // The selectors used for each ply.
    private MoveGenerator generator;        // The MoveGenerator being used
    private ProtocolHandler handler;        // The ProtocolHandler-object associated

    // TODO: Fix these:

    private boolean useFixedDepth = false;
    private long noOfNodes;
    private int timeForThisMove;

    private int pollForStopInterval = TIME_CHECK_INTERVAL;
    private boolean shouldWeStop = false;
    private long timeStarted;
    public long nodesSearched, qsearched;

    // Functions

    public Search(ProtocolHandler handler) {

        // TODO: Beginning of AbstractSearch

        // Sets the ProtocolHandler
        this.handler = handler;

        // Initializes the instance
        generator = MoveGenerator.getInstance();

        // Initializes the PlyInfo instances that will be used.
        selectors = new MoveSelector[MAX_PLY];

        for (int a = 0; a < MAX_PLY; a++) {
            selectors[a] = new MoveSelector();
        }

        // Initialize transposition table: TODO: Move me?
        transTable = new TranspositionTable();
        transTable.initialize();

        // TODO: End of AbstractSearch

    }

    // Couldn't resist to steal the "think" name from Glaurung
    public int think(Board board, int depth, int nodes) {

        // Time management

        // TODO: Add "fixed time".

        useFixedDepth = (depth != 0);

        if (!useFixedDepth) {
            timeForThisMove = handler.getTimeForThisMove(board.sideToMove);
        }

        // Initialization

        nodesSearched = 0;
        qsearched = 0;

        shouldWeStop = false;
        timeStarted = System.currentTimeMillis();

        int iteration = 1;
        int bestMove = Move.MOVE_NONE;
        int alpha = -Value.INFINITY, beta = Value.INFINITY;

        while (iteration < 60 /* TODO: FIX */) {

            // Reset the stop poll interval:
            pollForStopInterval = TIME_CHECK_INTERVAL;

            int eval = searchRoot(board, iteration, alpha, beta);

            // Checks to see if we should stop.
            if (shouldWeStop) break;

            // Check if aspiration window failed, and if so: search with a wider window:

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

            // TODO: info depth 88 score mate 2 time 17 nodes 10107 nps 594529 pv h6h7 h8h7 h5g6 

            System.out.printf("info score cp %d depth %d nodes %d nps %d time %d pv %s\n",
                    eval, iteration, nodesSearched, nps, timeUsed, sb.toString());
            bestMove = pvArray[0];

            // Adjusts the aspiration window size
            alpha = Math.max(eval - ASPIRATION_SIZE, -Value.INFINITY);
            beta = Math.min(eval + ASPIRATION_SIZE, Value.INFINITY);

            // Break if we are using fixed depth and we have searched to that specific depth:
            if (useFixedDepth && iteration == depth) break;

            iteration++;
        }

        assert bestMove != Move.MOVE_NONE;
        return bestMove;
    }

    public int searchRoot(Board board, int depth, int alpha, int beta) {

        int eval = 0;
        int bestEval = -Value.INFINITY;
        int evalType = HASH_ALPHA;
        int bestMove = Move.MOVE_NONE;

        MoveSelector selector = selectors[0];
        selector.initialize(board, Move.MOVE_NONE, board.isCheck(), false); // Not a PV search

        int move;
        while ((move = selector.getNextMove()) != Move.MOVE_NONE) {

            if (shouldWeStop) break;

            board.make(move);

            if (evalType == HASH_EXACT) { // Do a PV-search
                eval = -alphaBeta(board, depth - 1, 1, -alpha - 1, -alpha, true, true);

                if (eval > alpha && eval < beta) {
                    eval = -alphaBeta(board, depth - 1, 1, -beta, -alpha, true, true);
                }
            } else {
                eval = -alphaBeta(board, depth - 1, 1, -beta, -alpha, true, true);
            }

            board.retract(move);

            if (eval > bestEval) {
                if (eval >= beta) {
                    // Beta cutoff!
                    if (!shouldWeStop) {
                        transTable.put(board.key, HASH_BETA, depth, eval, move);
                    }

                    return beta;
                }

                bestEval = eval;
                bestMove = move;

                if (eval > alpha) {
                    evalType = HASH_EXACT;
                    alpha = eval;
                }
            }
        }

        // Checks for mate or stalemate:
        if (bestMove == Move.MOVE_NONE) {
            // TODO: do something if (stale)mate?
        } else {
            transTable.put(board.key, evalType, depth, eval, bestMove);
        }

        return alpha;
    }

    public int alphaBeta(Board board, int depth, int ply, int alpha, int beta,
                         boolean nmAllowed, boolean mcAllowed) {

        if (!useFixedDepth && --pollForStopInterval == 0) {
            pollForStopInterval = TIME_CHECK_INTERVAL;

            if (System.currentTimeMillis() > (timeStarted + timeForThisMove)) {
                System.out.printf("Time finished: %d. Movetime: %d. Time started: %d.\n",
                        System.currentTimeMillis(), timeForThisMove, timeStarted);
                shouldWeStop = true;
            }
        }

        nodesSearched++; // Increment the nodes searched!

        // TODO: Repetition detection!

        // Check in the transposition table
        TranspositionTable.HashEntry entry = transTable.get(board.key);

        if (entry != null && entry.depth >= depth) {
            if (entry.type == HASH_EXACT) {
                return entry.eval;
            } else if (entry.type == HASH_ALPHA && entry.eval <= alpha) {
                return entry.eval; // TODO: change to return alpha?
            } else if (entry.type == HASH_BETA && entry.eval >= beta) {
                return entry.eval; // TODO: change to return beta?
            }
        }

        int eval;

        // Horizon?
        if (depth <= 0) {
            eval = qsearch(board, ply, alpha, beta);
            //eval = Evaluation.evaluate(board, false);            
            transTable.putLeaf(board.key, eval, alpha, beta);
            return eval;
        }

        // Move generation (TODO: move sorting)

        int move;
        boolean isCheck = board.isCheck();
        MoveSelector selector = selectors[ply];
        selector.initialize(board, (entry == null ? Move.MOVE_NONE : entry.move), isCheck, false);

        // Checks for mate or stalemate

        if ((move = selector.getNextMove()) == Move.MOVE_NONE) {
            return isCheck ? (-Value.MATE + ply) : Value.DRAW;
        }

        // Null move pruning

        if (DO_NULL_MOVES && nmAllowed && depth >= 2 && !isCheck
                && board.info.material[board.sideToMove] > board.info.pawnMaterial[board.sideToMove]) {

            int epSquare = board.epSquare;
            board.makeNullMove();

            eval = -alphaBeta(board, depth - NULL_MOVE_REDUCTION, ply + 1, -beta, -beta + 1, false, !mcAllowed);

            board.retractNullMove(epSquare);

            if (eval >= beta) {
                if (!shouldWeStop) {
                    transTable.put(board.key, HASH_BETA, depth, eval, move);
                }
                return eval;
            }
        }

        if (shouldWeStop) {
            return 0;
        }

        int bestEval = -Value.INFINITY, bestMove = Move.MOVE_NONE, evalType = HASH_ALPHA;

        int[] moveQueue = new int[MC_EXPAND];
        moveQueue[0] = move;
        int queueIndex = 1, queueSize = 0;

        if (DO_MULTI_CUT && depth >= MC_REDUCTION && mcAllowed) {

            int c = 0;

            for (; queueIndex < MC_EXPAND; queueIndex++) {

                board.make(move);
                eval = -alphaBeta(board, depth - 1 - MC_REDUCTION, ply + 1, -beta, -alpha, true, !mcAllowed);
                board.retract(move);

                if (eval >= beta) {
                    c++;
                    if (c == MC_CUTOFFS) {
                        //System.out.println("mc-prune occurred!");
                        return beta;
                    }
                }

                move = selector.getNextMove();
                if (move == Move.MOVE_NONE) break;
                moveQueue[queueIndex] = move;
            }

            queueSize = queueIndex;
            queueIndex = 0;
            move = moveQueue[queueIndex++];
        }

        int movesSearched = 0;

        while (true) {

            board.make(move);

            if (evalType == HASH_EXACT) { // Do a PV-search

                // Late Move Reduction

                int type = Move.getType(move);
                if (DO_LMR && movesSearched >= LMR_FULL_DEPTH_MOVES && depth >= 3 && !isCheck
                        && !Move.isCapture(type) && !Move.isPromotion(type)) {
                    eval = -alphaBeta(board, depth - 2, ply + 1, -alpha - 1, -alpha, true, !mcAllowed);
                } else {
                    eval = alpha + 1;
                }

                if (eval > alpha) {
                    eval = -alphaBeta(board, depth - 1, ply + 1, -alpha - 1, -alpha, true, !mcAllowed);

                    if (eval > alpha && eval < beta) {
                        eval = -alphaBeta(board, depth - 1, ply + 1, -beta, -alpha, true, !mcAllowed);
                    }
                }
            } else {
                eval = -alphaBeta(board, depth - 1, ply + 1, -beta, -alpha, true, !mcAllowed);
            }

            board.retract(move);

            movesSearched++;

            if (eval > bestEval) {
                if (eval >= beta) {

                    // Beta cutoff!
                    if (!shouldWeStop) {
                        transTable.put(board.key, HASH_BETA, depth, eval, move);
                    }

                    return beta;
                }
                bestEval = eval;
                bestMove = move;

                if (eval > alpha) {
                    evalType = HASH_EXACT;
                    alpha = eval;
                }
            }

            if (!shouldWeStop) {
                transTable.put(board.key, evalType, depth, bestEval, bestMove);
            }

            // Get then next move from the queue or from the move stack, if the queue is empty:
            if (queueSize <= queueIndex) {
                move = selector.getNextMove();
                if (move == Move.MOVE_NONE) break;
            } else {
                move = moveQueue[queueIndex++];
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

}